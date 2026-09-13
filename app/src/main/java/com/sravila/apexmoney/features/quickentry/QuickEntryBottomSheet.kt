package com.sravila.apexmoney.features.quickentry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sravila.apexmoney.core.database.TransactionEntity
import com.sravila.apexmoney.core.ui.ApexButton
import com.sravila.apexmoney.core.ui.SegmentedControl
import com.sravila.apexmoney.core.database.AccountEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEntryBottomSheet(
    onDismissRequest: () -> Unit,
    onSaveTransaction: (TransactionEntity) -> Unit,
    currencySymbol: String = "$",
    dynamicCategories: List<String> = emptyList(),
    accounts: List<AccountEntity> = emptyList()
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedTypeIndex by remember { mutableStateOf(0) } // 0 = EXPENSE, 1 = INCOME, 2 = TRANSFER
    val isExpense = selectedTypeIndex == 0
    val isIncome = selectedTypeIndex == 1
    val isTransfer = selectedTypeIndex == 2

    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(if (isExpense) "Alimentos y Mercado" else if (isIncome) "Salario / Honorarios" else "Transferencia") }
    var noteText by remember { mutableStateOf("") }
    
    var selectedAccount by remember(accounts) { mutableStateOf(accounts.firstOrNull()) }
    var destinationAccount by remember(accounts) { mutableStateOf(accounts.firstOrNull { it != selectedAccount } ?: accounts.firstOrNull()) }

    var selectedDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var selectedTime by remember { mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }

    val defaultExpenseCategories = listOf("Alimentos y Mercado", "Tecnología & Gadgets", "Transporte & Gasolina", "Suscripciones & Ocio", "Restaurantes & Cafés", "Salud & Cuidado", "Otros")
    val expenseCategories = if (dynamicCategories.isNotEmpty()) dynamicCategories else defaultExpenseCategories
    
    val incomeCategories = listOf("Salario / Honorarios", "Inversión & Rendimientos", "Venta / Negocio", "Reembolso", "Otros Ingresos")
    val transferCategories = listOf("Transferencia Bancaria", "Retiro Efectivo", "Depósito Efectivo")

    val categories = if (isExpense) expenseCategories else if (isIncome) incomeCategories else transferCategories

    // Date Picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val date = LocalDate.of(year, month + 1, dayOfMonth)
            selectedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Time Picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val time = LocalTime.of(hourOfDay, minute)
            selectedTime = time.format(DateTimeFormatter.ofPattern("HH:mm"))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Captura Rápida Express",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selector Tipo (Gasto / Ingreso / Transferencia)
            SegmentedControl(
                items = listOf("Gasto", "Ingreso", "Transfer"),
                selectedIndex = selectedTypeIndex,
                onItemSelected = { index ->
                    selectedTypeIndex = index
                    selectedCategory = if (index == 0) expenseCategories[0] else if (index == 1) incomeCategories[0] else transferCategories[0]
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Monto estilo Calculadora Financiera
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountText = input
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto ($currencySymbol)") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Categoría
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipBg)
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = chipText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selectores de Cuentas
            Text(
                text = if (isTransfer) "Cuenta Origen" else "Cuenta",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { acc ->
                    val isSelected = acc == selectedAccount
                    val chipBg = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    val chipText = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { selectedAccount = acc }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = acc.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = chipText
                        )
                    }
                }
            }

            if (isTransfer) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cuenta Destino",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts.filter { it != selectedAccount }) { acc ->
                        val isSelected = acc == destinationAccount
                        val chipBg = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        val chipText = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .clickable { destinationAccount = acc }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = acc.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = chipText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Fecha y Hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fecha
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = selectedDate, style = MaterialTheme.typography.bodyMedium)
                }

                // Hora
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = selectedTime, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nota u Observación
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nota u Observación (Opcional)") },
                placeholder = { Text("Ej. Almuerzo de trabajo, Taxi, etc.") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar
            ApexButton(
                text = "Registrar Transacción",
                icon = Icons.Default.Check,
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && selectedAccount != null) {
                        val transaction = TransactionEntity(
                            type = if (isExpense) "EXPENSE" else if (isIncome) "INCOME" else "TRANSFER",
                            amount = amount,
                            category = selectedCategory,
                            date = selectedDate,
                            time = selectedTime,
                            note = noteText.ifBlank { null },
                            accountId = selectedAccount!!.id,
                            destinationAccountId = if (isTransfer) destinationAccount?.id else null
                        )
                        onSaveTransaction(transaction)
                        onDismissRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && selectedAccount != null && (!isTransfer || destinationAccount != null)
            )
        }
    }
}
