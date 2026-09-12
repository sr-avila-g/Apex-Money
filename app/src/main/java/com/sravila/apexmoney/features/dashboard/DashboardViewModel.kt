package com.sravila.apexmoney.features.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sravila.apexmoney.core.database.ApexMoneyDatabase
import com.sravila.apexmoney.core.database.TransactionEntity
import com.sravila.apexmoney.core.datastore.UserPreferences
import com.sravila.apexmoney.core.datastore.UserPreferencesRepository
import com.sravila.apexmoney.core.utils.FinancialHealthCalculator
import com.sravila.apexmoney.core.utils.FinancialSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: FinancialSummary = FinancialSummary(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val userPreferences: UserPreferences? = null,
    val isLoading: Boolean = false,
    val budgetCategories: List<String> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ApexMoneyDatabase.getDatabase(application)
    private val prefsRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

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
                val recent = transactions.take(10)
                DashboardUiState(
                    summary = summary,
                    recentTransactions = recent,
                    userPreferences = prefs,
                    isLoading = false,
                    budgetCategories = budgets.map { it.categoryName }
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.transactionDao().insertTransaction(transaction)
        }
    }
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.transactionDao().softDeleteTransaction(transactionId)
        }
    }

    fun toggleDiscreetMode(currentValue: Boolean) {
        viewModelScope.launch {
            prefsRepository.updateBooleanPreference("discreet", !currentValue)
        }
    }
}
