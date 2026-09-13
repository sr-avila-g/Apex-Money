package com.sravila.apexmoney.core.utils

import com.sravila.apexmoney.core.database.BudgetCategoryEntity
import com.sravila.apexmoney.core.database.SavingsVaultEntity
import com.sravila.apexmoney.core.database.TransactionEntity

data class FinancialSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val savingsRate: Float = 0f,
    val totalSavings: Double = 0.0,
    val totalBudgetLimit: Double = 0.0,
    val budgetSpentPercentage: Float = 0f,
    val topExpenseCategory: String = "Ninguno",
    val healthScore: Int = 100 // 0 to 100 score
)

object FinancialHealthCalculator {

    fun calculate(
        transactions: List<TransactionEntity>,
        budgets: List<BudgetCategoryEntity>,
        vaults: List<SavingsVaultEntity>
    ): FinancialSummary {
        val totalIncome = transactions
            .filter { it.type == "INCOME" }
            .sumOf { it.amount }

        val totalExpenses = transactions
            .filter { it.type == "EXPENSE" }
            .sumOf { it.amount }

        val netBalance = totalIncome - totalExpenses

        val savingsRate = if (totalIncome > 0) {
            ((netBalance / totalIncome) * 100).toFloat().coerceIn(0f, 100f)
        } else {
            0f
        }

        val totalSavings = vaults.sumOf { it.currentAmount }
        val totalBudgetLimit = budgets.sumOf { it.monthlyLimit }

        val budgetedExpenses = transactions.filter { tx ->
            tx.type == "EXPENSE" && budgets.any { it.categoryName.equals(tx.category, ignoreCase = true) }
        }.sumOf { it.amount }

        val budgetSpentPercentage = if (totalBudgetLimit > 0) {
            (budgetedExpenses / totalBudgetLimit).toFloat().coerceIn(0f, 2f)
        } else {
            0f
        }

        val topCategoryEntry = transactions
            .filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .maxByOrNull { entry -> entry.value.sumOf { it.amount } }

        val topCategory = topCategoryEntry?.key ?: "Sin Gastos"

        // Health score computation (0 to 100)
        var score = 100
        if (netBalance < 0) score -= 40
        if (savingsRate < 10f) score -= 20
        if (budgetSpentPercentage > 1.0f) score -= 30
        else if (budgetSpentPercentage > 0.85f) score -= 15

        return FinancialSummary(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netBalance = netBalance,
            savingsRate = savingsRate,
            totalSavings = totalSavings,
            totalBudgetLimit = totalBudgetLimit,
            budgetSpentPercentage = budgetSpentPercentage,
            topExpenseCategory = topCategory,
            healthScore = score.coerceIn(0, 100)
        )
    }
}
