package com.sravila.apexmoney.features.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sravila.apexmoney.core.theme.ExpenseRed
import com.sravila.apexmoney.core.ui.ApexButton

data class AccountType(
    val key: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isCreditCard: Boolean = false
)

val accountTypes = listOf(
    AccountType("CASH", "Efectivo", Icons.Outlined.AccountBalanceWallet),
    AccountType("BANK_ACCOUNT", "Banco", Icons.Outlined.AccountBalance),
    AccountType("DIGITAL_WALLET", "Digital", Icons.Outlined.PhoneAndroid),
    AccountType("CREDIT_CARD", "Tarjeta TC", Icons.Outlined.CreditCard, isCreditCard = true)
)

val accountColors = listOf(
    "#3B82F6", // Azul
    "#4CAF50", // Verde
    "#FF2A2A", // Rojo
    "#FFB703", // Amarillo
    "#AB47BC", // Morado
    "#FFFFFF"  // Blanco
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountBottomSheet(
    onDismissRequest: () -> Unit,
    onSave: (name: String, type: String, initialBalance: Double, colorHex: String, isCreditCard: Boolean) -> Unit,
    isCreditCardSupportEnabled: Boolean = true,
    existingAccount: com.sravila.apexmoney.core.database.AccountEntity? = null
) {
    var name by remember { mutableStateOf(existingAccount?.name ?: "") }
    var selectedType by remember {
        mutableStateOf(
            accountTypes.firstOrNull { it.key == existingAccount?.type } ?: accountTypes[0]
        )
    }
    var balanceText by remember {
        mutableStateOf(
            if (existingAccount != null && !existingAccount.isCreditCard)
                existingAccount.initialBalance.toString()
            else ""
        )
    }
    var selectedColor by remember { mutableStateOf(existingAccount?.colorHex ?: accountColors[0]) }

    val isEditing = existingAccount != null
    val visibleTypes = if (isCreditCardSupportEnabled) accountTypes else accountTypes.dropLast(1)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Editar Cuenta" else "Nueva Cuenta",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Outlined.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de la cuenta") },
                placeholder = { Text("Ej. Banco BNB, PayPal, Billetera") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tipo de cuenta
            Text(
                text = "Tipo de Cuenta",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                visibleTypes.forEach { type ->
                    val isSelected = selectedType.key == type.key
                    val chipColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipColor)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedType = type }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = null,
                            tint = if (type.isCreditCard && isSelected) ExpenseRed
                                  else if (isSelected) MaterialTheme.colorScheme.onPrimary
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                    }
                }
            }

            // Badge TC
            if (selectedType.isCreditCard) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ExpenseRed.copy(alpha = 0.12f))
                        .border(1.dp, ExpenseRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "💳 Tarjeta de Crédito — El saldo representa tu deuda actual. El saldo inicial siempre parte en $0.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ExpenseRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Saldo inicial (bloqueado para TC)
            OutlinedTextField(
                value = if (selectedType.isCreditCard) "0.00" else balanceText,
                onValueChange = { input ->
                    if (!selectedType.isCreditCard) {
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            balanceText = input
                        }
                    }
                },
                enabled = !selectedType.isCreditCard,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (selectedType.isCreditCard) "Saldo Inicial (bloqueado en TC)" else "Saldo Inicial") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Color de etiqueta
            Text(
                text = "Color de Etiqueta",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                accountColors.forEach { colorHex ->
                    val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.White }
                    val isSelected = selectedColor == colorHex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .clickable { selectedColor = colorHex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón guardar
            ApexButton(
                text = if (isEditing) "GUARDAR CAMBIOS" else "CREAR CUENTA",
                icon = if (isEditing) Icons.Outlined.AccountBalance else Icons.Outlined.AccountBalanceWallet,
                onClick = {
                    val balance = if (selectedType.isCreditCard) 0.0 else (balanceText.toDoubleOrNull() ?: 0.0)
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedType.key, balance, selectedColor, selectedType.isCreditCard)
                        onDismissRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            )
        }
    }
}
