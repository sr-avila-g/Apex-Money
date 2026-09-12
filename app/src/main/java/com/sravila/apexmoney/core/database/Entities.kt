package com.sravila.apexmoney.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm
    val note: String? = null,
    val account: String? = "Efectivo",
    val isRecurring: Boolean = false,
    val updatedAt: String = java.time.Instant.now().toString(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "budgets")
data class BudgetCategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val categoryName: String,
    val monthlyLimit: Double,
    val iconName: String = "Category",
    val colorHex: String = "#FF2A42",
    val updatedAt: String = java.time.Instant.now().toString(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "savings_vaults")
data class SavingsVaultEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val vaultName: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: String? = null,
    val category: String = "Fondo General",
    val iconName: String = "Vault",
    val updatedAt: String = java.time.Instant.now().toString(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "recurring_payments")
data class RecurringPaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val frequency: String = "MENSUAL", // MENSUAL, ANUAL, SEMANAL
    val dueDate: String, // YYYY-MM-DD
    val category: String = "Suscripciones",
    val isPaidThisMonth: Boolean = false,
    val updatedAt: String = java.time.Instant.now().toString(),
    val isDeleted: Boolean = false
)
