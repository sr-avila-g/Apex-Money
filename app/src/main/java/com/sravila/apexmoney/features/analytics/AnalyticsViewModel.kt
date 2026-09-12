package com.sravila.apexmoney.features.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sravila.apexmoney.core.database.ApexMoneyDatabase
import com.sravila.apexmoney.core.datastore.UserPreferences
import com.sravila.apexmoney.core.datastore.UserPreferencesRepository
import com.sravila.apexmoney.core.utils.FinancialHealthCalculator
import com.sravila.apexmoney.core.utils.FinancialSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CategorySpendShare(
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class AnalyticsUiState(
    val summary: FinancialSummary = FinancialSummary(),
    val categoryShares: List<CategorySpendShare> = emptyList(),
    val totalTransactionsCount: Int = 0,
    val userPreferences: UserPreferences? = null
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ApexMoneyDatabase.getDatabase(application)
    private val prefsRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                database.transactionDao().getAllTransactionsFlow(),
                database.budgetDao().getAllBudgetsFlow(),
                database.vaultDao().getAllVaultsFlow(),
                prefsRepository.userPreferencesFlow
            ) { transactions, budgets, vaults, prefs ->
                val summary = FinancialHealthCalculator.calculate(transactions, budgets, vaults)

                val expenseTxs = transactions.filter { it.type == "EXPENSE" }
                val totalExp = summary.totalExpenses

                val categoryShares = expenseTxs
                    .groupBy { it.category }
                    .map { (cat, txs) ->
                        val sum = txs.sumOf { it.amount }
                        val pct = if (totalExp > 0) (sum / totalExp).toFloat() else 0f
                        CategorySpendShare(cat, sum, pct)
                    }
                    .sortedByDescending { it.amount }

                AnalyticsUiState(
                    summary = summary,
                    categoryShares = categoryShares,
                    totalTransactionsCount = transactions.size,
                    userPreferences = prefs
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
