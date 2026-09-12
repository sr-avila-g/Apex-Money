package com.sravila.apexmoney.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC, time DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC, time DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC, time DESC LIMIT :limit")
    suspend fun getRecentTransactions(limit: Int = 10): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND date >= :startDate AND date <= :endDate")
    suspend fun getTransactionsBetween(startDate: String, endDate: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions WHERE id IN (:ids)")
    suspend fun getTransactionsByIds(ids: List<String>): List<TransactionEntity>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteTransaction(id: String, updatedAt: String = java.time.Instant.now().toString())

    @Query("SELECT * FROM transactions WHERE updatedAt > :timestamp")
    suspend fun getTransactionsModifiedSince(timestamp: String): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE isDeleted = 1")
    fun countDeletedTransactionsFlow(): Flow<Int>

    @Query("DELETE FROM transactions WHERE isDeleted = 1")
    suspend fun emptyTrashTransactions()

    @Query("DELETE FROM transactions WHERE isDeleted = 1 AND updatedAt < :thresholdDate")
    suspend fun purgeOldDeletedTransactions(thresholdDate: String)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE isDeleted = 0 ORDER BY categoryName ASC")
    fun getAllBudgetsFlow(): Flow<List<BudgetCategoryEntity>>

    @Query("SELECT * FROM budgets WHERE isDeleted = 0 ORDER BY categoryName ASC")
    suspend fun getAllBudgets(): List<BudgetCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetCategoryEntity>)

    @Query("SELECT * FROM budgets WHERE id IN (:ids)")
    suspend fun getBudgetsByIds(ids: List<String>): List<BudgetCategoryEntity>

    @Update
    suspend fun updateBudget(budget: BudgetCategoryEntity)

    @Query("UPDATE budgets SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteBudget(id: String, updatedAt: String = java.time.Instant.now().toString())

    @Query("SELECT * FROM budgets WHERE updatedAt > :timestamp")
    suspend fun getBudgetsModifiedSince(timestamp: String): List<BudgetCategoryEntity>

    @Query("SELECT COUNT(*) FROM budgets WHERE isDeleted = 1")
    fun countDeletedBudgetsFlow(): Flow<Int>

    @Query("DELETE FROM budgets WHERE isDeleted = 1")
    suspend fun emptyTrashBudgets()

    @Query("DELETE FROM budgets WHERE isDeleted = 1 AND updatedAt < :thresholdDate")
    suspend fun purgeOldDeletedBudgets(thresholdDate: String)
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM savings_vaults WHERE isDeleted = 0 ORDER BY vaultName ASC")
    fun getAllVaultsFlow(): Flow<List<SavingsVaultEntity>>

    @Query("SELECT * FROM savings_vaults WHERE isDeleted = 0 ORDER BY vaultName ASC")
    suspend fun getAllVaults(): List<SavingsVaultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVault(vault: SavingsVaultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaults(vaults: List<SavingsVaultEntity>)

    @Query("SELECT * FROM savings_vaults WHERE id IN (:ids)")
    suspend fun getVaultsByIds(ids: List<String>): List<SavingsVaultEntity>

    @Update
    suspend fun updateVault(vault: SavingsVaultEntity)

    @Query("UPDATE savings_vaults SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteVault(id: String, updatedAt: String = java.time.Instant.now().toString())

    @Query("SELECT * FROM savings_vaults WHERE updatedAt > :timestamp")
    suspend fun getVaultsModifiedSince(timestamp: String): List<SavingsVaultEntity>

    @Query("SELECT COUNT(*) FROM savings_vaults WHERE isDeleted = 1")
    fun countDeletedVaultsFlow(): Flow<Int>

    @Query("DELETE FROM savings_vaults WHERE isDeleted = 1")
    suspend fun emptyTrashVaults()

    @Query("DELETE FROM savings_vaults WHERE isDeleted = 1 AND updatedAt < :thresholdDate")
    suspend fun purgeOldDeletedVaults(thresholdDate: String)
}

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_payments WHERE isDeleted = 0 ORDER BY dueDate ASC")
    fun getAllRecurringFlow(): Flow<List<RecurringPaymentEntity>>

    @Query("SELECT * FROM recurring_payments WHERE isDeleted = 0 ORDER BY dueDate ASC")
    suspend fun getAllRecurring(): List<RecurringPaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: RecurringPaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurrings(recurrings: List<RecurringPaymentEntity>)

    @Query("SELECT * FROM recurring_payments WHERE id IN (:ids)")
    suspend fun getRecurringByIds(ids: List<String>): List<RecurringPaymentEntity>

    @Update
    suspend fun updateRecurring(recurring: RecurringPaymentEntity)

    @Query("UPDATE recurring_payments SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteRecurring(id: String, updatedAt: String = java.time.Instant.now().toString())

    @Query("SELECT * FROM recurring_payments WHERE updatedAt > :timestamp")
    suspend fun getRecurringModifiedSince(timestamp: String): List<RecurringPaymentEntity>

    @Query("SELECT COUNT(*) FROM recurring_payments WHERE isDeleted = 1")
    fun countDeletedRecurringFlow(): Flow<Int>

    @Query("DELETE FROM recurring_payments WHERE isDeleted = 1")
    suspend fun emptyTrashRecurring()

    @Query("DELETE FROM recurring_payments WHERE isDeleted = 1 AND updatedAt < :thresholdDate")
    suspend fun purgeOldDeletedRecurring(thresholdDate: String)
}
