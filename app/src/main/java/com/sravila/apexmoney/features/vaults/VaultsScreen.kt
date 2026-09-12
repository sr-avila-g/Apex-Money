package com.sravila.apexmoney.features.vaults

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
import com.sravila.apexmoney.core.database.SavingsVaultEntity
import com.sravila.apexmoney.core.theme.IncomeGreen
import com.sravila.apexmoney.core.ui.*
import com.sravila.apexmoney.core.utils.CurrencyFormatter

@Composable
fun VaultsScreen(
    viewModel: VaultsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = uiState.userPreferences?.currencySymbol ?: "$"

    var showAddDialog by remember { mutableStateOf(false) }
    var vaultToDeposit by remember { mutableStateOf<SavingsVaultEntity?>(null) }
    var vaultToWithdraw by remember { mutableStateOf<SavingsVaultEntity?>(null) }

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
                        text = "Bóvedas",
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
                        contentDescription = "Agregar Bóveda",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Total Saved Card
        item {
            ObsidianCard(
                borderColor = IncomeGreen.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "TOTAL ACUMULADO EN BÓVEDAS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                MoneyDisplay(
                    amount = uiState.totalSaved,
                    currencySymbol = currencySymbol,
                    isIncome = true,
                    fontSize = 28
                )

                Spacer(modifier = Modifier.height(12.dp))
                val pct = if (uiState.totalTarget > 0) (uiState.totalSaved / uiState.totalTarget).toFloat() else 0f
                Text(
                    text = "Meta Global: ${CurrencyFormatter.format(uiState.totalTarget, currencySymbol)} (${(pct * 100).toInt()}% alcanzado)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                ProgressBarModular(progress = pct, barColor = IncomeGreen)
            }
        }

        item {
            SectionHeader(title = "Tus Bóvedas de Ahorro")
        }

        if (uiState.vaults.isEmpty()) {
            item {
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes bóvedas de ahorro activas. Toca + para crear tu primer objetivo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.vaults, key = { it.id }) { vault ->
                VaultItemRow(
                    vault = vault,
                    currencySymbol = currencySymbol,
                    onDeposit = { vaultToDeposit = vault },
                    onWithdraw = { vaultToWithdraw = vault },
                    onDelete = { viewModel.deleteVault(vault.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddVaultDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, target, initial, cat ->
                viewModel.addVault(name, target, initial, cat)
                showAddDialog = false
            },
            currencySymbol = currencySymbol
        )
    }

    vaultToDeposit?.let { vault ->
        DepositDialog(
            vault = vault,
            onDismiss = { vaultToDeposit = null },
            onConfirm = { amount ->
                viewModel.depositToVault(vault, amount)
                vaultToDeposit = null
            },
            currencySymbol = currencySymbol
        )
    }

    vaultToWithdraw?.let { vault ->
        WithdrawDialog(
            vault = vault,
            onDismiss = { vaultToWithdraw = null },
            onConfirm = { amount ->
                viewModel.withdrawFromVault(vault, amount)
                vaultToWithdraw = null
            },
            currencySymbol = currencySymbol
        )
    }
}

@Composable
fun VaultItemRow(
    vault: SavingsVaultEntity,
    currencySymbol: String,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onDelete: () -> Unit
) {
    val pct = if (vault.targetAmount > 0) (vault.currentAmount / vault.targetAmount).toFloat() else 0f
    val isCompleted = vault.currentAmount >= vault.targetAmount

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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = if (isCompleted) IncomeGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = vault.vaultName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = vault.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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

            Spacer(modifier = Modifier.height(12.dp))
            ProgressBarModular(
                progress = pct,
                barColor = if (isCompleted) IncomeGreen else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${CurrencyFormatter.format(vault.currentAmount, currencySymbol)} de ${CurrencyFormatter.format(vault.targetAmount, currencySymbol)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row {
                    TextButton(onClick = onWithdraw) {
                        Text("Retirar", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    TextButton(onClick = onDeposit) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aportar", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun AddVaultDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, String) -> Unit,
    currencySymbol: String
) {
    var vaultName by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initialText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Bóveda de Ahorro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = vaultName,
                    onValueChange = { vaultName = it },
                    label = { Text("Nombre de la Meta (ej. Nuevo Auto)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            targetText = input
                        }
                    },
                    label = { Text("Monto Objetivo ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = initialText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            initialText = input
                        }
                    },
                    label = { Text("Aporte Inicial ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: 0.0
                    val initial = initialText.toDoubleOrNull() ?: 0.0
                    if (vaultName.isNotBlank() && target > 0) {
                        onConfirm(vaultName.trim(), target, initial, category)
                    }
                },
                enabled = vaultName.isNotBlank() && (targetText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Crear Bóveda")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DepositDialog(
    vault: SavingsVaultEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    currencySymbol: String
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aportar a ${vault.vaultName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Monto acumulado actual: ${CurrencyFormatter.format(vault.currentAmount, currencySymbol)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = input
                        }
                    },
                    label = { Text("Monto del Aporte ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Confirmar Aporte")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun WithdrawDialog(
    vault: SavingsVaultEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    currencySymbol: String
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Retirar de ${vault.vaultName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Monto acumulado actual: ${CurrencyFormatter.format(vault.currentAmount, currencySymbol)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = input
                        }
                    },
                    label = { Text("Monto a retirar ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Confirmar Retiro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
