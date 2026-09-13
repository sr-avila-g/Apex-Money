package com.sravila.apexmoney.features.vaults

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sravila.apexmoney.core.database.ApexMoneyDatabase
import com.sravila.apexmoney.core.database.SavingsVaultEntity
import com.sravila.apexmoney.core.datastore.UserPreferences
import com.sravila.apexmoney.core.datastore.UserPreferencesRepository
import com.sravila.apexmoney.core.database.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class VaultsUiState(
    val vaults: List<SavingsVaultEntity> = emptyList(),
    val totalSaved: Double = 0.0,
    val totalTarget: Double = 0.0,
    val userPreferences: UserPreferences? = null
)

class VaultsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ApexMoneyDatabase.getDatabase(application)
    private val prefsRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(VaultsUiState())
    val uiState: StateFlow<VaultsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                database.vaultDao().getAllVaultsFlow(),
                prefsRepository.userPreferencesFlow
            ) { vaults, prefs ->
                val saved = vaults.sumOf { it.currentAmount }
                val target = vaults.sumOf { it.targetAmount }
                VaultsUiState(
                    vaults = vaults,
                    totalSaved = saved,
                    totalTarget = target,
                    userPreferences = prefs
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addVault(vaultName: String, targetAmount: Double, initialAmount: Double, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.vaultDao().insertVault(
                SavingsVaultEntity(
                    vaultName = vaultName,
                    targetAmount = targetAmount,
                    currentAmount = initialAmount,
                    category = category
                )
            )
        }
    }

    fun depositToVault(vault: SavingsVaultEntity, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = vault.copy(
                currentAmount = vault.currentAmount + amount,
                updatedAt = java.time.Instant.now().toString()
            )
            database.vaultDao().updateVault(updated)
            
            // Registramos la salida de dinero hacia la Bóveda
            val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            database.transactionDao().insertTransaction(
                TransactionEntity(
                    type = "EXPENSE",
                    amount = amount,
                    category = "Transferencia a Bóveda",
                    date = date,
                    time = time,
                    note = "Depósito en bóveda: ${vault.vaultName}",
                    accountId = "acc_main"
                )
            )
        }
    }

    fun withdrawFromVault(vault: SavingsVaultEntity, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = vault.copy(
                currentAmount = vault.currentAmount - amount,
                updatedAt = java.time.Instant.now().toString()
            )
            database.vaultDao().updateVault(updated)
            
            // Registramos la entrada de dinero desde la Bóveda al balance
            val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            database.transactionDao().insertTransaction(
                TransactionEntity(
                    type = "INCOME",
                    amount = amount,
                    category = "Retiro de Bóveda",
                    date = date,
                    time = time,
                    note = "Retiro de bóveda: ${vault.vaultName}",
                    accountId = "acc_main"
                )
            )
        }
    }

    fun deleteVault(vaultId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.vaultDao().softDeleteVault(vaultId)
        }
    }
}
