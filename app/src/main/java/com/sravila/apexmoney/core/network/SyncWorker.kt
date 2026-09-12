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
            val lastSync = userPrefs.lastSyncTimestamp
            
            // 1. Capture exact sync start time (Race condition fix)
            val syncStartTime = Instant.now().toString()

            // 2. PUSH (Local -> Remote)
            pushTransactions(db, lastSync)
            pushBudgets(db, lastSync)
            pushVaults(db, lastSync)
            pushRecurring(db, lastSync)

            // 3. PULL (Remote -> Local) with Conflict Resolution
            pullTransactions(db, lastSync)
            pullBudgets(db, lastSync)
            pullVaults(db, lastSync)
            pullRecurring(db, lastSync)

            // Update last sync time with the exact start time
            preferencesRepo.updateLastSync(syncStartTime)

            // 4. Purge 30-day old deleted records safely (Local)
            purgeOldTrash(db, lastSync, syncStartTime)
            
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during sync: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun pushTransactions(db: ApexMoneyDatabase, lastSync: String) {
        val modified = db.transactionDao().getTransactionsModifiedSince(lastSync)
        if (modified.isNotEmpty()) {
            val dtos = modified.map { it.toDto() }
            SupabaseApi.client.postgrest["transactions"].upsert(dtos)
        }
    }

    private suspend fun pullTransactions(db: ApexMoneyDatabase, lastSync: String) {
        val remote = SupabaseApi.client.postgrest["transactions"]
            .select { filter { gt("updated_at", lastSync) } }
            .decodeList<TransactionDto>()
        
        remote.forEach { dto ->
            db.transactionDao().insertTransaction(dto.toEntity())
        }
    }

    private suspend fun pushBudgets(db: ApexMoneyDatabase, lastSync: String) {
        val modified = db.budgetDao().getBudgetsModifiedSince(lastSync)
        if (modified.isNotEmpty()) {
            val dtos = modified.map { it.toDto() }
            SupabaseApi.client.postgrest["budgets"].upsert(dtos)
        }
    }

    private suspend fun pullBudgets(db: ApexMoneyDatabase, lastSync: String) {
        val remote = SupabaseApi.client.postgrest["budgets"]
            .select { filter { gt("updated_at", lastSync) } }
            .decodeList<BudgetDto>()
        
        remote.forEach { dto ->
            db.budgetDao().insertBudget(dto.toEntity())
        }
    }

    private suspend fun pushVaults(db: ApexMoneyDatabase, lastSync: String) {
        val modified = db.vaultDao().getVaultsModifiedSince(lastSync)
        if (modified.isNotEmpty()) {
            val dtos = modified.map { it.toDto() }
            SupabaseApi.client.postgrest["savings_vaults"].upsert(dtos)
        }
    }

    private suspend fun pullVaults(db: ApexMoneyDatabase, lastSync: String) {
        val remote = SupabaseApi.client.postgrest["savings_vaults"]
            .select { filter { gt("updated_at", lastSync) } }
            .decodeList<SavingsVaultDto>()
        
        remote.forEach { dto ->
            db.vaultDao().insertVault(dto.toEntity())
        }
    }

    private suspend fun pushRecurring(db: ApexMoneyDatabase, lastSync: String) {
        val modified = db.recurringDao().getRecurringModifiedSince(lastSync)
        if (modified.isNotEmpty()) {
            val dtos = modified.map { it.toDto() }
            SupabaseApi.client.postgrest["recurring_payments"].upsert(dtos)
        }
    }

    private suspend fun pullRecurring(db: ApexMoneyDatabase, lastSync: String) {
        val remote = SupabaseApi.client.postgrest["recurring_payments"]
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
