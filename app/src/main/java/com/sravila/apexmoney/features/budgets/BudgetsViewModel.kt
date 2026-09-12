package com.sravila.apexmoney.features.budgets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sravila.apexmoney.core.database.ApexMoneyDatabase
import com.sravila.apexmoney.core.database.BudgetCategoryEntity
import com.sravila.apexmoney.core.datastore.UserPreferences
import com.sravila.apexmoney.core.datastore.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class BudgetCategoryStatus(
    val budget: BudgetCategoryEntity,
    val spentAmount: Double,
    val spentPercentage: Float
)

data class BudgetsUiState(
    val budgetStatuses: List<BudgetCategoryStatus> = emptyList(),
    val totalBudgetLimit: Double = 0.0,
    val totalSpent: Double = 0.0,
    val userPreferences: UserPreferences? = null
)

class BudgetsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ApexMoneyDatabase.getDatabase(application)
    private val prefsRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(BudgetsUiState())
    val uiState: StateFlow<BudgetsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                database.budgetDao().getAllBudgetsFlow(),
                database.transactionDao().getAllTransactionsFlow(),
                prefsRepository.userPreferencesFlow
            ) { budgets, transactions, prefs ->
                val expenseTxs = transactions.filter { it.type == "EXPENSE" }

                val statuses = budgets.map { b ->
                    val spent = expenseTxs.filter { it.category.equals(b.categoryName, ignoreCase = true) }
                        .sumOf { it.amount }
                    val pct = if (b.monthlyLimit > 0) (spent / b.monthlyLimit).toFloat() else 0f
                    BudgetCategoryStatus(b, spent, pct)
                }

                val totalLimit = budgets.sumOf { it.monthlyLimit }
                val totalSpent = statuses.sumOf { it.spentAmount }

                BudgetsUiState(
                    budgetStatuses = statuses,
                    totalBudgetLimit = totalLimit,
                    totalSpent = totalSpent,
                    userPreferences = prefs
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addBudgetCategory(categoryName: String, monthlyLimit: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            database.budgetDao().insertBudget(
                BudgetCategoryEntity(
                    categoryName = categoryName,
                    monthlyLimit = monthlyLimit
                )
            )
        }
    }

    fun deleteBudgetCategory(budgetId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.budgetDao().softDeleteBudget(budgetId)
        }
    }
}
