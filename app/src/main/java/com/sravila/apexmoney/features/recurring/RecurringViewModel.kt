package com.sravila.apexmoney.features.recurring

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sravila.apexmoney.core.database.ApexMoneyDatabase
import com.sravila.apexmoney.core.database.RecurringPaymentEntity
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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import java.util.concurrent.TimeUnit
import java.time.temporal.ChronoUnit

data class RecurringUiState(
    val recurringPayments: List<RecurringPaymentEntity> = emptyList(),
    val totalMonthlyRecurring: Double = 0.0,
    val totalPendingAmount: Double = 0.0,
    val userPreferences: UserPreferences? = null
)

class RecurringViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ApexMoneyDatabase.getDatabase(application)
    private val prefsRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                database.recurringDao().getAllRecurringFlow(),
                prefsRepository.userPreferencesFlow
            ) { payments, prefs ->
                val total = payments.sumOf { it.amount }
                val pending = payments.filter { !it.isPaidThisMonth }.sumOf { it.amount }
                RecurringUiState(
                    recurringPayments = payments,
                    totalMonthlyRecurring = total,
                    totalPendingAmount = pending,
                    userPreferences = prefs
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun togglePaidStatus(payment: RecurringPaymentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val isNowPaid = !payment.isPaidThisMonth
            val updated = payment.copy(
                isPaidThisMonth = isNowPaid,
                updatedAt = java.time.Instant.now().toString()
            )
            database.recurringDao().updateRecurring(updated)
            
            val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            
            if (isNowPaid) {
                // Si lo marca como pagado, registramos el gasto
                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        type = "EXPENSE",
                        amount = payment.amount,
                        category = payment.category,
                        date = date,
                        time = time,
                        note = "Pago recurrente: ${payment.title}",
                        isRecurring = true,
                        accountId = "acc_main"
                    )
                )
            } else {
                // Si lo desmarca, revertimos el gasto con un ingreso
                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        type = "INCOME",
                        amount = payment.amount,
                        category = payment.category,
                        date = date,
                        time = time,
                        note = "Reverso suscripción: ${payment.title}",
                        isRecurring = true,
                        accountId = "acc_main"
                    )
                )
            }
        }
    }

    fun addRecurringPayment(title: String, amount: Double, dueDate: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.recurringDao().insertRecurring(
                RecurringPaymentEntity(
                    title = title,
                    amount = amount,
                    dueDate = dueDate,
                    category = category
                )
            )
            
            // Programar notificación
            try {
                val targetDate = LocalDate.parse(dueDate)
                val now = LocalDate.now()
                val daysUntilDue = ChronoUnit.DAYS.between(now, targetDate)
                
                if (daysUntilDue >= 0) {
                    val inputData = Data.Builder()
                        .putString("PAYMENT_TITLE", title)
                        .putDouble("PAYMENT_AMOUNT", amount)
                        .build()
                        
                    val workRequest = OneTimeWorkRequestBuilder<PaymentNotificationWorker>()
                        .setInitialDelay(daysUntilDue, TimeUnit.DAYS)
                        .setInputData(inputData)
                        .build()
                        
                    WorkManager.getInstance(getApplication()).enqueue(workRequest)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteRecurring(paymentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.recurringDao().softDeleteRecurring(paymentId)
        }
    }
}
