package com.sravila.apexmoney.features.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sravila.apexmoney.core.database.TransactionEntity
import com.sravila.apexmoney.core.theme.ExpenseRed
import com.sravila.apexmoney.core.theme.IncomeGreen
import com.sravila.apexmoney.core.theme.WarningYellow
import com.sravila.apexmoney.core.ui.*
import com.sravila.apexmoney.core.utils.CurrencyFormatter

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenQuickEntry: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToVaults: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAccountDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = uiState.userPreferences?.currencySymbol ?: "$"
    val isDiscreetModeEnabled = uiState.userPreferences?.isDiscreetMode ?: false
    var isBalanceHidden by remember(isDiscreetModeEnabled) { mutableStateOf(isDiscreetModeEnabled) }
    val isDiscreetMode = isBalanceHidden
    val summary = uiState.summary
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header del Centro de Mando
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Centro de Mando",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Health Score Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                summary.healthScore >= 80 -> IncomeGreen.copy(alpha = 0.15f)
                                summary.healthScore >= 50 -> WarningYellow.copy(alpha = 0.15f)
                                else -> ExpenseRed.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = null,
                            tint = when {
                                summary.healthScore >= 80 -> IncomeGreen
                                summary.healthScore >= 50 -> WarningYellow
                                else -> ExpenseRed
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${summary.healthScore}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                BounceIconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Ajustes",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 2. Tarjeta Principal de Balance (Obsidian Card Hero)
        item {
            ObsidianCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BALANCE DISPONIBLE NETO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isDiscreetModeEnabled) {
                        BounceIconButton(
                            onClick = { isBalanceHidden = !isBalanceHidden },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceHidden) androidx.compose.material.icons.Icons.Outlined.VisibilityOff else androidx.compose.material.icons.Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                MoneyDisplay(
                    amount = summary.netBalance,
                    currencySymbol = currencySymbol,
                    isDiscreetMode = isDiscreetMode,
                    fontSize = 32
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fila de Flujo de Caja (Ingresos vs Gastos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Ingresos
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowUpward,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ingresos Mes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        MoneyDisplay(
                            amount = summary.totalIncome,
                            currencySymbol = currencySymbol,
                            isDiscreetMode = isDiscreetMode,
                            isIncome = true,
                            fontSize = 18
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )

                    // Gastos
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowDownward,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Gastos Mes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        MoneyDisplay(
                            amount = summary.totalExpenses,
                            currencySymbol = currencySymbol,
                            isDiscreetMode = isDiscreetMode,
                            isIncome = false,
                            fontSize = 18
                        )
                    }
                }
            }
        }

        // 2.5. Cuentas (Billeteras y Bancos)
        if (uiState.userPreferences?.showAccountsCarousel != false) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Mis Cuentas",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (uiState.accounts.isEmpty()) {
                        ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No hay cuentas agregadas. Toca + para crear tu primera billetera o cuenta.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 16.dp) // extra padding so last item isn't cut off
                        ) {
                            items(uiState.accounts, key = { it.account.id }) { acc ->
                                val borderColor = if (uiState.userPreferences?.accountColorBadges != false) {
                                    Color(android.graphics.Color.parseColor(acc.account.colorHex)).copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                }
                                
                                ObsidianCard(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .bounceClick { onNavigateToAccountDetail(acc.account.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    borderColor = borderColor
                                ) {
                                    Text(
                                        text = acc.account.name,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    MoneyDisplay(
                                        amount = acc.currentBalance,
                                        currencySymbol = currencySymbol,
                                        isDiscreetMode = isDiscreetMode,
                                        fontSize = 18
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Resumen de Presupuestos & Tasa de Ahorro
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Presupuesto Consumido Card
                ObsidianCard(
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick { onNavigateToBudgets() }
                ) {
                    Text(
                        text = "PRESUPUESTO MES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(summary.budgetSpentPercentage * 100).toInt()}% Consumido",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.budgetSpentPercentage > 0.9f) ExpenseRed else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressBarModular(
                        progress = summary.budgetSpentPercentage,
                        barColor = if (summary.budgetSpentPercentage > 0.9f) ExpenseRed else MaterialTheme.colorScheme.primary
                    )
                }
                
                // Tasa de Ahorro Card
                ObsidianCard(
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick { onNavigateToVaults() }
                ) {
                    Text(
                        text = "AHORRO ACUMULADO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isDiscreetMode) "⬤⬤⬤⬤" else CurrencyFormatter.formatCompact(summary.totalSavings, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = IncomeGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tasa: ${summary.savingsRate.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 4. Botón de Captura Rápida Destacado
        item {
            ApexButton(
                text = "Registrar Transacción Rápida",
                icon = Icons.Outlined.Add,
                onClick = onOpenQuickEntry,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 5. Transacciones Recientes Header
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            SectionHeader(
                title = "Transacciones Recientes",
                actionText = "Ver Todo",
                onAction = { 
                    android.widget.Toast.makeText(context, "Historial completo próximamente...", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
        
        // 6. Lista de Transacciones Recientes
        if (uiState.recentTransactions.isEmpty()) {
            item {
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay transacciones registradas este mes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.recentTransactions, key = { it.id }) { tx ->
                TransactionItemRow(
                    transaction = tx,
                    currencySymbol = currencySymbol,
                    isDiscreetMode = isDiscreetMode,
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    isDiscreetMode: Boolean,
    transaction: TransactionEntity,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"
    
    ObsidianCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon Category Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.type == "TRANSFER") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else if (isIncome) IncomeGreen.copy(alpha = 0.15f)
                            else ExpenseRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.type == "TRANSFER") Icons.Outlined.SwapHoriz else if (isIncome) Icons.Outlined.TrendingUp else Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = if (transaction.type == "TRANSFER") MaterialTheme.colorScheme.primary else if (isIncome) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${transaction.date} • ${transaction.time}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!transaction.note.isNullOrBlank()) {
                        Text(
                            text = transaction.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoneyDisplay(
                    amount = transaction.amount,
                    currencySymbol = currencySymbol,
                    isDiscreetMode = isDiscreetMode,
                    isIncome = isIncome,
                    showSign = true,
                    fontSize = 16
                )
                BounceIconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
