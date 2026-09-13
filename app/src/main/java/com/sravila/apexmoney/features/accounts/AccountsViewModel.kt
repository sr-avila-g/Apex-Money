package com.sravila.apexmoney.features.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sravila.apexmoney.core.database.AccountEntity
import com.sravila.apexmoney.core.database.AccountWithBalance
import com.sravila.apexmoney.core.database.ApexMoneyDatabase
import com.sravila.apexmoney.core.database.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AccountDetailUiState(
    val account: AccountWithBalance? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = true
)

class AccountsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ApexMoneyDatabase.getDatabase(application)

    private val _detailState = MutableStateFlow(AccountDetailUiState())
    val detailState: StateFlow<AccountDetailUiState> = _detailState.asStateFlow()

    fun loadAccountDetail(accountId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                database.accountDao().getAccountsWithBalanceFlow(),
                database.accountDao().getTransactionsByAccountFlow(accountId)
            ) { accounts, transactions ->
                val account = accounts.firstOrNull { it.account.id == accountId }
                AccountDetailUiState(
                    account = account,
                    transactions = transactions,
                    isLoading = false
                )
            }.collect { state ->
                _detailState.value = state
            }
        }
    }

    fun addAccount(
        name: String,
        type: String,
        initialBalance: Double,
        colorHex: String,
        isCreditCard: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = AccountEntity(
                name = name,
                type = type,
                initialBalance = if (isCreditCard) 0.0 else initialBalance,
                colorHex = colorHex,
                iconName = typeToIcon(type),
                isCreditCard = isCreditCard
            )
            database.accountDao().insertAccount(account)
        }
    }

    fun editAccount(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.accountDao().updateAccount(
                account.copy(updatedAt = java.time.Instant.now().toString())
            )
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.accountDao().softDeleteAccount(accountId)
        }
    }

    private fun typeToIcon(type: String): String = when (type) {
        "BANK_ACCOUNT" -> "AccountBalance"
        "DIGITAL_WALLET" -> "PhoneAndroid"
        "CREDIT_CARD" -> "CreditCard"
        else -> "AccountBalanceWallet" // CASH
    }
}
