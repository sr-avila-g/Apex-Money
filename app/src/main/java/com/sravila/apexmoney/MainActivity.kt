package com.sravila.apexmoney

import com.sravila.apexmoney.core.network.SyncManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sravila.apexmoney.core.datastore.UserPreferencesRepository
import com.sravila.apexmoney.core.theme.ApexMoneyTheme
import com.sravila.apexmoney.core.theme.ThemeMode
import com.sravila.apexmoney.features.analytics.AnalyticsScreen
import com.sravila.apexmoney.features.analytics.AnalyticsViewModel
import com.sravila.apexmoney.features.budgets.BudgetsScreen
import com.sravila.apexmoney.features.budgets.BudgetsViewModel
import com.sravila.apexmoney.features.budgets.AddBudgetDialog
import com.sravila.apexmoney.features.dashboard.DashboardScreen
import com.sravila.apexmoney.features.dashboard.DashboardViewModel
import com.sravila.apexmoney.features.quickentry.CreationMenuBottomSheet
import com.sravila.apexmoney.features.quickentry.QuickEntryBottomSheet
import com.sravila.apexmoney.features.recurring.RecurringScreen
import com.sravila.apexmoney.features.recurring.RecurringViewModel
import com.sravila.apexmoney.features.recurring.AddRecurringDialog
import com.sravila.apexmoney.features.settings.SettingsScreen
import com.sravila.apexmoney.features.settings.SettingsViewModel
import com.sravila.apexmoney.features.vaults.VaultsScreen
import com.sravila.apexmoney.features.vaults.VaultsViewModel
import com.sravila.apexmoney.features.vaults.AddVaultDialog
import com.sravila.apexmoney.ui.navigation.ApexMoneyBottomBar
import com.sravila.apexmoney.ui.navigation.NavItem
import com.sravila.apexmoney.ui.splash.AnimatedSplashScreen

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SyncManager.schedulePeriodicSync(applicationContext)

        setContent {
            val prefsRepository = remember { UserPreferencesRepository(applicationContext) }
            val userPrefs by prefsRepository.userPreferencesFlow.collectAsState(initial = null)
            val themeMode = userPrefs?.themeMode ?: ThemeMode.AMOLED
            val isBiometricEnabled = userPrefs?.isBiometricEnabled == true

            var showSplash by remember { mutableStateOf(true) }
            var isAuthenticated by remember { mutableStateOf(false) }
            val context = LocalContext.current

            LaunchedEffect(isBiometricEnabled, isAuthenticated) {
                if (isBiometricEnabled && !isAuthenticated) {
                    val executor = ContextCompat.getMainExecutor(context)
                    val biometricPrompt = BiometricPrompt(this@MainActivity, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                                    finish() // Close app if user cancels
                                }
                            }

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                isAuthenticated = true
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                            }
                        })
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Apex Money")
                        .setSubtitle("Autenticación Requerida")
                        .setNegativeButtonText("Salir")
                        .build()
                    biometricPrompt.authenticate(promptInfo)
                }
            }

            ApexMoneyTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isBiometricEnabled && !isAuthenticated) {
                        // Empty screen or Lock icon while waiting for biometrics
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Bloqueado", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        }
                    } else if (showSplash) {
                        AnimatedSplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    } else {
                        ApexMoneyMainContent(
                            userCurrencySymbol = userPrefs?.currencySymbol ?: "$"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApexMoneyMainContent(
    userCurrencySymbol: String
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavItem.Dashboard.route

    val dashboardViewModel: DashboardViewModel = viewModel()
    val budgetsViewModel: BudgetsViewModel = viewModel()
    val vaultsViewModel: VaultsViewModel = viewModel()
    val recurringViewModel: RecurringViewModel = viewModel()
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    var showCreationMenu by remember { mutableStateOf(false) }
    var showQuickEntrySheet by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddVaultDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            ApexMoneyBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(NavItem.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onQuickEntryClick = { showCreationMenu = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreationMenu = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Captura Rápida"
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavItem.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            enterTransition = {
                scaleIn(initialScale = 0.9f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                scaleOut(targetScale = 1.1f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                scaleIn(initialScale = 1.1f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                scaleOut(targetScale = 0.9f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(NavItem.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onOpenQuickEntry = { showQuickEntrySheet = true },
                    onNavigateToBudgets = { navController.navigate(NavItem.Budgets.route) },
                    onNavigateToVaults = { navController.navigate(NavItem.Vaults.route) },
                    onNavigateToSettings = { navController.navigate(NavItem.Settings.route) }
                )
            }

            composable(NavItem.Budgets.route) {
                BudgetsScreen(viewModel = budgetsViewModel)
            }

            composable(NavItem.Vaults.route) {
                VaultsScreen(viewModel = vaultsViewModel)
            }

            composable(NavItem.Analytics.route) {
                AnalyticsScreen(
                    viewModel = analyticsViewModel,
                    onNavigateToSettings = { navController.navigate(NavItem.Settings.route) }
                )
            }
            
            composable(NavItem.Recurring.route) {
                RecurringScreen(viewModel = recurringViewModel)
            }

            composable(NavItem.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showQuickEntrySheet) {
        val uiState by dashboardViewModel.uiState.collectAsState()
        QuickEntryBottomSheet(
            onDismissRequest = { showQuickEntrySheet = false },
            onSaveTransaction = { transaction ->
                dashboardViewModel.addTransaction(transaction)
            },
            currencySymbol = userCurrencySymbol,
            dynamicCategories = uiState.budgetCategories
        )
    }

    if (showCreationMenu) {
        CreationMenuBottomSheet(
            onDismissRequest = { showCreationMenu = false },
            onSelectTransaction = { showCreationMenu = false; showQuickEntrySheet = true },
            onSelectBudget = { showCreationMenu = false; showAddBudgetDialog = true },
            onSelectVault = { showCreationMenu = false; showAddVaultDialog = true },
            onSelectRecurring = { showCreationMenu = false; showAddRecurringDialog = true }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { name, amount ->
                budgetsViewModel.addBudgetCategory(name, amount)
                showAddBudgetDialog = false
            },
            currencySymbol = userCurrencySymbol
        )
    }

    if (showAddVaultDialog) {
        AddVaultDialog(
            onDismiss = { showAddVaultDialog = false },
            onConfirm = { name, target, initial, category ->
                vaultsViewModel.addVault(name, target, initial, category)
                showAddVaultDialog = false
            },
            currencySymbol = userCurrencySymbol
        )
    }

    if (showAddRecurringDialog) {
        AddRecurringDialog(
            onDismiss = { showAddRecurringDialog = false },
            onConfirm = { title, amount, dueDate, category ->
                recurringViewModel.addRecurringPayment(title, amount, dueDate, category)
                showAddRecurringDialog = false
            },
            currencySymbol = userCurrencySymbol
        )
    }
}
