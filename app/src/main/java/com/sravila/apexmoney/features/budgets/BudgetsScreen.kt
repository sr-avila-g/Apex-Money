package com.sravila.apexmoney.features.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sravila.apexmoney.core.theme.ExpenseRed
import com.sravila.apexmoney.core.theme.IncomeGreen
import com.sravila.apexmoney.core.theme.WarningYellow
import com.sravila.apexmoney.core.ui.*
import com.sravila.apexmoney.core.utils.CurrencyFormatter

@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = uiState.userPreferences?.currencySymbol ?: "$"

    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Presupuestos",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Agregar Presupuesto",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Summary Hero Card
        item {
            ObsidianCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "LÍMITE TOTAL MENSUAL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                MoneyDisplay(
                    amount = uiState.totalBudgetLimit,
                    currencySymbol = currencySymbol,
                    fontSize = 28
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Gastado: ${CurrencyFormatter.format(uiState.totalSpent, currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (uiState.totalSpent > uiState.totalBudgetLimit) ExpenseRed else IncomeGreen
                    )
                    Text(
                        text = "Disponible: ${CurrencyFormatter.format((uiState.totalBudgetLimit - uiState.totalSpent).coerceAtLeast(0.0), currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SectionHeader(title = "Categorías Configuradas")
        }

        if (uiState.budgetStatuses.isEmpty()) {
            item {
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes presupuestos configurados. Toca el botón + para añadir uno.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.budgetStatuses, key = { it.budget.id }) { status ->
                BudgetItemRow(
                    status = status,
                    currencySymbol = currencySymbol,
                    onDelete = { viewModel.deleteBudgetCategory(status.budget.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, limit ->
                viewModel.addBudgetCategory(name, limit)
                showAddDialog = false
            },
            currencySymbol = currencySymbol
        )
    }
}

@Composable
fun BudgetItemRow(
    status: BudgetCategoryStatus,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    val pct = status.spentPercentage
    val barColor = when {
        pct >= 1.0f -> ExpenseRed
        pct >= 0.75f -> WarningYellow
        else -> IncomeGreen
    }

    ObsidianCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = status.budget.categoryName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = barColor
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            ProgressBarModular(progress = pct, barColor = barColor)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gastado: ${CurrencyFormatter.format(status.spentAmount, currencySymbol)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Límite: ${CurrencyFormatter.format(status.budget.monthlyLimit, currencySymbol)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit,
    currencySymbol: String
) {
    var categoryName by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Presupuesto", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Categoría (ej. Ropa, Juegos)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            limitText = input
                        }
                    },
                    label = { Text("Límite Mensual ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    if (categoryName.isNotBlank() && limit > 0) {
                        onConfirm(categoryName.trim(), limit)
                    }
                },
                enabled = categoryName.isNotBlank() && (limitText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
