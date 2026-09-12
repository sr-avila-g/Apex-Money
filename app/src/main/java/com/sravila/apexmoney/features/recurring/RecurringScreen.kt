package com.sravila.apexmoney.features.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.sravila.apexmoney.core.database.RecurringPaymentEntity
import com.sravila.apexmoney.core.theme.ExpenseRed
import com.sravila.apexmoney.core.theme.IncomeGreen
import com.sravila.apexmoney.core.theme.WarningYellow
import com.sravila.apexmoney.core.ui.*
import com.sravila.apexmoney.core.utils.CurrencyFormatter

@Composable
fun RecurringScreen(
    viewModel: RecurringViewModel,
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
                        text = "Recurrentes",
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
                        contentDescription = "Agregar Pago Recurrente",
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
                    text = "TOTAL COMPROMETIDO MENSUAL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                MoneyDisplay(
                    amount = uiState.totalMonthlyRecurring,
                    currencySymbol = currencySymbol,
                    fontSize = 28
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pendiente este mes: ${CurrencyFormatter.format(uiState.totalPendingAmount, currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (uiState.totalPendingAmount > 0) WarningYellow else IncomeGreen
                    )
                }
            }
        }

        item {
            SectionHeader(title = "Tus Cobros Programados")
        }

        if (uiState.recurringPayments.isEmpty()) {
            item {
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes pagos recurrentes ni suscripciones registradas. Toca + para agregar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.recurringPayments, key = { it.id }) { payment ->
                RecurringItemRow(
                    payment = payment,
                    currencySymbol = currencySymbol,
                    onTogglePaid = { viewModel.togglePaidStatus(payment) },
                    onDelete = { viewModel.deleteRecurring(payment.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddRecurringDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amount, dueDate, category ->
                viewModel.addRecurringPayment(title, amount, dueDate, category)
                showAddDialog = false
            },
            currencySymbol = currencySymbol
        )
    }
}

@Composable
fun RecurringItemRow(
    payment: RecurringPaymentEntity,
    currencySymbol: String,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    ObsidianCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
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
                Checkbox(
                    checked = payment.isPaidThisMonth,
                    onCheckedChange = { onTogglePaid() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = IncomeGreen,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = payment.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (payment.isPaidThisMonth) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Vence: ${payment.dueDate} • ${payment.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                MoneyDisplay(
                    amount = payment.amount,
                    currencySymbol = currencySymbol,
                    isIncome = null,
                    fontSize = 16
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
    }
}

@Composable
fun AddRecurringDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String) -> Unit,
    currencySymbol: String
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("Día 15 de cada mes") }
    var category by remember { mutableStateOf("Suscripciones") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Suscripción / Pago Fijo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del Pago (ej. Netflix, Alquiler)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = input
                        }
                    },
                    label = { Text("Monto Mensual ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Fecha o Día de Cobro") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(title.trim(), amount, dueDate.trim(), category)
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Guardar Pago")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
