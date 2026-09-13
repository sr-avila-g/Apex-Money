package com.sravila.apexmoney.features.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sravila.apexmoney.core.datastore.UserPreferences
import com.sravila.apexmoney.core.datastore.UserPreferencesRepository
import com.sravila.apexmoney.core.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepository = UserPreferencesRepository(application)

    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    private val _trashCount = MutableStateFlow(0)
    val trashCount: StateFlow<Int> = _trashCount.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.userPreferencesFlow.collect { prefs ->
                _userPreferences.value = prefs
            }
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            val db = com.sravila.apexmoney.core.database.ApexMoneyDatabase.getDatabase(application)
            kotlinx.coroutines.flow.combine(
                db.transactionDao().countDeletedTransactionsFlow(),
                db.budgetDao().countDeletedBudgetsFlow(),
                db.vaultDao().countDeletedVaultsFlow(),
                db.recurringDao().countDeletedRecurringFlow()
            ) { t, b, v, r ->
                t + b + v + r
            }.collect { total ->
                _trashCount.value = total
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.updateThemeMode(themeMode)
        }
    }

    fun setCurrency(symbol: String, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.updateCurrency(symbol, code)
        }
    }

    fun togglePreference(key: String, currentValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.updateBooleanPreference(key, !currentValue)
        }
    }

    fun updateSupabaseCredentials(url: String, key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.updateSupabaseCredentials(url, key)
        }
    }

    fun wipeData(onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                com.sravila.apexmoney.core.database.ApexMoneyDatabase.getDatabase(getApplication()).clearAllTables()
                launch(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    fun emptyTrash(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Delete from Supabase
                val prefs = _userPreferences.value
                val isCloudEnabled = prefs?.isCloudSyncEnabled ?: false
                
                if (isCloudEnabled) {
                    val client = com.sravila.apexmoney.core.network.SupabaseApi.getClient(
                        prefs?.customSupabaseUrl ?: "",
                        prefs?.customSupabaseKey ?: ""
                    )
                    val postgrest = client.postgrest
                    
                    postgrest["transactions"].delete { filter { eq("is_deleted", true) } }
                    postgrest["budgets"].delete { filter { eq("is_deleted", true) } }
                    postgrest["savings_vaults"].delete { filter { eq("is_deleted", true) } }
                    postgrest["recurring_payments"].delete { filter { eq("is_deleted", true) } }
                }

                // Delete from local
                val db = com.sravila.apexmoney.core.database.ApexMoneyDatabase.getDatabase(getApplication())
                db.transactionDao().emptyTrashTransactions()
                db.budgetDao().emptyTrashBudgets()
                db.vaultDao().emptyTrashVaults()
                db.recurringDao().emptyTrashRecurring()

                launch(Dispatchers.Main) { onResult(true, "Papelera vaciada correctamente") }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { onResult(false, "No se pudo vaciar la nube. Verifica tu conexión.") }
            }
        }
    }
    
    fun exportData(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = com.sravila.apexmoney.core.database.ApexMoneyDatabase.getDatabase(context)
                val transactions = db.transactionDao().getAllTransactions()
                val csvHeader = "id,note,amount,date,time,category,type,isRecurring\n"
                val csvData = transactions.joinToString(separator = "\n") {
                    "${it.id},${it.note ?: ""},${it.amount},${it.date},${it.time},${it.category},${it.type},${it.isRecurring}"
                }
                
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(downloadsDir, "apexmoney_backup_${System.currentTimeMillis()}.csv")
                file.writeText(csvHeader + csvData)
                
                launch(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Exportado en Descargas: ${file.name}", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Error al exportar", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    fun importData(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val files = downloadsDir.listFiles { _, name -> name.startsWith("apexmoney_backup_") && name.endsWith(".csv") }
                if (files.isNullOrEmpty()) {
                    launch(Dispatchers.Main) { android.widget.Toast.makeText(context, "No se encontraron backups", android.widget.Toast.LENGTH_SHORT).show() }
                    return@launch
                }
                
                val latestFile = files.maxByOrNull { it.lastModified() }
                if (latestFile != null) {
                    val lines = latestFile.readLines()
                    if (lines.size > 1) {
                        val db = com.sravila.apexmoney.core.database.ApexMoneyDatabase.getDatabase(context)
                        for (i in 1 until lines.size) {
                            val parts = lines[i].split(",")
                            if (parts.size >= 8) {
                                val t = com.sravila.apexmoney.core.database.TransactionEntity(
                                    id = parts[0],
                                    note = parts[1],
                                    amount = parts[2].toDoubleOrNull() ?: 0.0,
                                    date = parts[3],
                                    time = parts[4],
                                    category = parts[5],
                                    type = parts[6],
                                    isRecurring = parts[7].toBooleanStrictOrNull() ?: false,
                                    accountId = "acc_main",
                                    updatedAt = java.time.Instant.now().toString(),
                                    isDeleted = false
                                )
                                db.transactionDao().insertTransaction(t)
                            }
                        }
                        launch(Dispatchers.Main) { android.widget.Toast.makeText(context, "Importado desde ${latestFile.name}", android.widget.Toast.LENGTH_LONG).show() }
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { android.widget.Toast.makeText(context, "Error al importar", android.widget.Toast.LENGTH_SHORT).show() }
            }
        }
    }
}
