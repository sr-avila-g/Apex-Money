package com.sravila.apexmoney.core.network

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sravila.apexmoney.core.database.ApexMoneyDatabase
import com.sravila.apexmoney.core.datastore.UserPreferencesRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.first
import java.time.Instant
import io.github.jan.supabase.SupabaseClient

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = ApexMoneyDatabase.getDatabase(applicationContext)
            val preferencesRepo = UserPreferencesRepository(applicationContext)

            // Wait for first emission of preferences to get lastSyncTimestamp
            val userPrefs = preferencesRepo.userPreferencesFlow.first()
            
            // 1. Capture exact sync start time (Race condition fix)
            val syncStartTime = Instant.now().toString()
            val lastSync = userPrefs.lastSyncTimestamp

            // 4. Purge 30-day old deleted records safely (Local)
            // We do this ALWAYS, even if cloud sync is disabled.
            purgeOldTrash(db, lastSync, syncStartTime)

            if (!userPrefs.isCloudSyncEnabled) {
                // Return early if cloud sync is disabled
                return Result.success()
            }

            val client = SupabaseApi.getClient(userPrefs.customSupabaseUrl, userPrefs.customSupabaseKey)

            // 2. PUSH (Local -> Remote)
            pushTransactions(client, db, lastSync)
            pushBudgets(client, db, lastSync)
            pushVaults(client, db, lastSync)
            pushRecurring(client, db, lastSync)

            // 3. PULL (Remote -> Local) with Conflict Resolution
            pullTransactions(client, db, lastSync)
            pullBudgets(client, db, lastSync)
            pullVaults(client, db, lastSync)
            pullRecurring(client, db, lastSync)

            // Update last sync time with the exact start time
            preferencesRepo.updateLastSync(syncStartTime)
            
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during sync: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun pushTransactions(client: SupabaseClient, db: ApexMoneyDatabase, lastSync: String) {
        val modified = db.transactionDao().getTransactionsModifiedSince(lastSync)
        if (modified.isNotEmpty()) {
            val dtos = modified.map { it.toDto() }
            client.postgrest["transactions"].upsert(dtos)
        }
    }

    private suspend fun pullTransactions(client: SupabaseClient, db: ApexMoneyDatabase, lastSync: String) {
        val remote = client.postgrest["transactions"]
            .select { filter { gt("updated_at", lastSync) } }
            .decodeList<TransactionDto>()
        
        remote.forEach { dto ->
            db.transactionDao().insertTransaction(dto.toEntity())
        }
    }

    private suspend fun pushBudgets(client: SupabaseClient, db: ApexMoneyDatabase, lastSync: String) {
        val modified = db.budgetDao().getBudgetsModifiedSince(lastSync)
        if (modified.isNotEmpty()) {
            val dtos = modified.map { it.toDto() }
            client.postgrest["budgets"].upsert(dtos)
        }
    }

    private suspend fun pullBudgets(client: SupabaseClient, db: ApexMoneyDatabase, lastSync: String) {
        val remote = client.postgrest["budgets"]
            .select { filter { gt("updated_at", lastSync) } }
            .decodeList<BudgetDto>()
        
        remote.forEach { dto ->
            db.budgetDao().insertBudget(dto.toEntity())
        }
    }

    private suspend fun pushVaults(client: SupabaseClient, db: ApexMoneyDatabase, lastSync: String) {
        val modified = db.vaultDao().getVaultsModifiedSince(lastSync)
        if (modified.isNotEmpty()) {
            val dtos = modified.map { it.toDto() }
            client.postgrest["savings_vaults"].upsert(dtos)
        }
    }

    private suspend fun pullVaults(client: SupabaseClient, db: ApexMoneyDatabase, lastSync: String) {
        val remote = client.postgrest["savings_vaults"]
            .select { filter { gt("updated_at", lastSync) } }
            .decodeList<SavingsVaultDto>()
        
        remote.forEach { dto ->
            db.vaultDao().insertVault(dto.toEntity())
        }
    }

    private suspend fun pushRecurring(client: SupabaseClient, db: ApexMoneyDatabase, lastSync: String) {
        val modified = db.recurringDao().getRecurringModifiedSince(lastSync)
        if (modified.isNotEmpty()) {
            val dtos = modified.map { it.toDto() }
            client.postgrest["recurring_payments"].upsert(dtos)
        }
    }

    private suspend fun pullRecurring(client: SupabaseClient, db: ApexMoneyDatabase, lastSync: String) {
        val remote = client.postgrest["recurring_payments"]
            .select { filter { gt("updated_at", lastSync) } }
            .decodeList<RecurringPaymentDto>()
        
        remote.forEach { dto ->
            db.recurringDao().insertRecurring(dto.toEntity())
        }
    }

    private suspend fun purgeOldTrash(db: ApexMoneyDatabase, lastSync: String, syncStartTime: String) {
        val thirtyDaysAgo = Instant.now().minus(java.time.Duration.ofDays(30)).toString()
        // Ensure we only delete things older than 30 days that have ALSO been successfully synced
        val thresholdDate = minOf(thirtyDaysAgo, lastSync.takeIf { it != "1970-01-01T00:00:00Z" } ?: thirtyDaysAgo)
        
        db.transactionDao().purgeOldDeletedTransactions(thresholdDate)
        db.budgetDao().purgeOldDeletedBudgets(thresholdDate)
        db.vaultDao().purgeOldDeletedVaults(thresholdDate)
        db.recurringDao().purgeOldDeletedRecurring(thresholdDate)
    }
}
