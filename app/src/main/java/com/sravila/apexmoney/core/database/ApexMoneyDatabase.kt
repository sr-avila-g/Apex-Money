package com.sravila.apexmoney.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        TransactionEntity::class,
        BudgetCategoryEntity::class,
        SavingsVaultEntity::class,
        RecurringPaymentEntity::class,
        AccountEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class ApexMoneyDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun vaultDao(): VaultDao
    abstract fun recurringDao(): RecurringDao

    companion object {
        @Volatile
        private var INSTANCE: ApexMoneyDatabase? = null

        fun getDatabase(context: Context): ApexMoneyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ApexMoneyDatabase::class.java,
                    "apex_money_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial data asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    seedInitialData(database)
                }
            }

            private suspend fun seedInitialData(db: ApexMoneyDatabase) {
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

                // Presupuestos predeterminados
                val budgets = listOf(
                    BudgetCategoryEntity(categoryName = "Alimentos y Mercado", monthlyLimit = 400.0, colorHex = "#00E5FF", iconName = "ShoppingCart"),
                    BudgetCategoryEntity(categoryName = "Tecnología & Gadgets", monthlyLimit = 350.0, colorHex = "#FF2A42", iconName = "Laptop"),
                    BudgetCategoryEntity(categoryName = "Transporte & Gasolina", monthlyLimit = 150.0, colorHex = "#FFB703", iconName = "DirectionsCar"),
                    BudgetCategoryEntity(categoryName = "Suscripciones & Ocio", monthlyLimit = 120.0, colorHex = "#AB47BC", iconName = "Movie"),
                    BudgetCategoryEntity(categoryName = "Restaurantes & Cafés", monthlyLimit = 200.0, colorHex = "#FF7043", iconName = "Restaurant")
                )
                budgets.forEach { db.budgetDao().insertBudget(it) }

                // Bóvedas de ahorro predeterminadas
                val vaults = listOf(
                    SavingsVaultEntity(vaultName = "Fondo de Emergencia (6 Meses)", targetAmount = 5000.0, currentAmount = 2850.0, category = "Seguridad", iconName = "Shield"),
                    SavingsVaultEntity(vaultName = "Nuevo Setup / Workstation", targetAmount = 1800.0, currentAmount = 1200.0, category = "Tecnología", iconName = "Computer"),
                    SavingsVaultEntity(vaultName = "Viaje a Japón 2027", targetAmount = 4500.0, currentAmount = 950.0, category = "Viajes", iconName = "FlightTakeoff")
                )
                vaults.forEach { db.vaultDao().insertVault(it) }

                // Pagos recurrentes predeterminados
                val recurring = listOf(
                    RecurringPaymentEntity(title = "Servicio de Internet Fibra", amount = 45.0, frequency = "MENSUAL", dueDate = "$today", category = "Servicios", isPaidThisMonth = true),
                    RecurringPaymentEntity(title = "Suscripción Supabase / Cloud", amount = 25.0, frequency = "MENSUAL", dueDate = "$today", category = "Suscripciones", isPaidThisMonth = false),
                    RecurringPaymentEntity(title = "Seguro Médico Privado", amount = 110.0, frequency = "MENSUAL", dueDate = "$today", category = "Salud", isPaidThisMonth = false)
                )
                recurring.forEach { db.recurringDao().insertRecurring(it) }

                // Cuentas iniciales predeterminadas
                val mainAccount = AccountEntity(name = "Cuenta Principal (Banco)", type = "BANK_ACCOUNT", initialBalance = 1500.0, colorHex = "#3B82F6", iconName = "AccountBalance", isCreditCard = false)
                val wallet = AccountEntity(name = "Billetera Física", type = "CASH", initialBalance = 200.0, colorHex = "#4CAF50", iconName = "AccountBalanceWallet", isCreditCard = false)
                val creditCard = AccountEntity(name = "Tarjeta de Crédito", type = "CREDIT_CARD", initialBalance = 0.0, colorHex = "#FF2A2A", iconName = "CreditCard", isCreditCard = true)
                
                db.accountDao().insertAccount(mainAccount)
                db.accountDao().insertAccount(wallet)
                db.accountDao().insertAccount(creditCard)

                // Transacciones iniciales de demostración
                val transactions = listOf(
                    TransactionEntity(type = "INCOME", amount = 3500.0, category = "Salario / Proyecto", date = today, time = timeNow, note = "Cobro de Honorarios Mensuales", accountId = mainAccount.id),
                    TransactionEntity(type = "EXPENSE", amount = 145.50, category = "Alimentos y Mercado", date = today, time = timeNow, note = "Supermercado Semanal", accountId = mainAccount.id),
                    TransactionEntity(type = "EXPENSE", amount = 45.0, category = "Suscripciones & Ocio", date = today, time = timeNow, note = "Suscripciones Cloud", accountId = creditCard.id),
                    TransactionEntity(type = "EXPENSE", amount = 32.0, category = "Transporte & Gasolina", date = today, time = timeNow, note = "Recarga de Combustible", accountId = wallet.id)
                )
                transactions.forEach { db.transactionDao().insertTransaction(it) }
            }
        }
    }
}
