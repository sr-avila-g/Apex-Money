package com.sravila.apexmoney.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.sravila.apexmoney.core.database.TransactionEntity
import com.sravila.apexmoney.core.database.BudgetCategoryEntity
import com.sravila.apexmoney.core.database.SavingsVaultEntity
import com.sravila.apexmoney.core.database.RecurringPaymentEntity


@Serializable
data class TransactionDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("amount") val amount: Double,
    @SerialName("category") val category: String,
    @SerialName("date") val date: String,
    @SerialName("time") val time: String,
    @SerialName("note") val note: String?,
    @SerialName("account") val account: String?,
    @SerialName("is_recurring") val isRecurring: Boolean,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)

@Serializable
data class BudgetDto(
    @SerialName("id") val id: String,
    @SerialName("category_name") val categoryName: String,
    @SerialName("monthly_limit") val monthlyLimit: Double,
    @SerialName("icon_name") val iconName: String,
    @SerialName("color_hex") val colorHex: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)

@Serializable
data class SavingsVaultDto(
    @SerialName("id") val id: String,
    @SerialName("vault_name") val vaultName: String,
    @SerialName("target_amount") val targetAmount: Double,
    @SerialName("current_amount") val currentAmount: Double,
    @SerialName("target_date") val targetDate: String?,
    @SerialName("category") val category: String,
    @SerialName("icon_name") val iconName: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)

@Serializable
data class RecurringPaymentDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("amount") val amount: Double,
    @SerialName("frequency") val frequency: String,
    @SerialName("due_date") val dueDate: String,
    @SerialName("category") val category: String,
    @SerialName("is_paid_this_month") val isPaidThisMonth: Boolean,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean
)


fun TransactionEntity.toDto() = TransactionDto(
    id = id, type = type, amount = amount, category = category, date = date, time = time,
    note = note, account = account, isRecurring = isRecurring, updatedAt = updatedAt, isDeleted = isDeleted
)

fun TransactionDto.toEntity() = TransactionEntity(
    id = id, type = type, amount = amount, category = category, date = date, time = time,
    note = note, account = account, isRecurring = isRecurring, updatedAt = updatedAt, isDeleted = isDeleted
)

fun BudgetCategoryEntity.toDto() = BudgetDto(
    id = id, categoryName = categoryName, monthlyLimit = monthlyLimit, iconName = iconName,
    colorHex = colorHex, updatedAt = updatedAt, isDeleted = isDeleted
)

fun BudgetDto.toEntity() = BudgetCategoryEntity(
    id = id, categoryName = categoryName, monthlyLimit = monthlyLimit, iconName = iconName,
    colorHex = colorHex, updatedAt = updatedAt, isDeleted = isDeleted
)

fun SavingsVaultEntity.toDto() = SavingsVaultDto(
    id = id, vaultName = vaultName, targetAmount = targetAmount, currentAmount = currentAmount,
    targetDate = targetDate, category = category, iconName = iconName, updatedAt = updatedAt, isDeleted = isDeleted
)

fun SavingsVaultDto.toEntity() = SavingsVaultEntity(
    id = id, vaultName = vaultName, targetAmount = targetAmount, currentAmount = currentAmount,
    targetDate = targetDate, category = category, iconName = iconName, updatedAt = updatedAt, isDeleted = isDeleted
)

fun RecurringPaymentEntity.toDto() = RecurringPaymentDto(
    id = id, title = title, amount = amount, frequency = frequency, dueDate = dueDate,
    category = category, isPaidThisMonth = isPaidThisMonth, updatedAt = updatedAt, isDeleted = isDeleted
)

fun RecurringPaymentDto.toEntity() = RecurringPaymentEntity(
    id = id, title = title, amount = amount, frequency = frequency, dueDate = dueDate,
    category = category, isPaidThisMonth = isPaidThisMonth, updatedAt = updatedAt, isDeleted = isDeleted
)
