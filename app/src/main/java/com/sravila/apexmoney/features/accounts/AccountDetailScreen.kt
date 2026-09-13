package com.sravila.apexmoney.features.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import com.sravila.apexmoney.core.database.AccountEntity
import com.sravila.apexmoney.core.database.TransactionEntity
import com.sravila.apexmoney.core.theme.ExpenseRed
import com.sravila.apexmoney.core.theme.IncomeGreen
import com.sravila.apexmoney.core.ui.MoneyDisplay
import com.sravila.apexmoney.core.ui.ObsidianCard
import com.sravila.apexmoney.core.ui.BounceIconButton
import com.sravila.apexmoney.core.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: String,
    viewModel: AccountsViewModel,
    currencySymbol: String,
    onNavigateBack: () -> Unit,
    onEditAccount: (AccountEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.detailState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(accountId) {
        viewModel.loadAccountDetail(accountId)
    }

    val accountWithBalance = uiState.account
    val account = accountWithBalance?.account
    val balance = accountWithBalance?.currentBalance ?: 0.0
    val accentColor = try {
        Color(android.graphics.Color.parseColor(account?.colorHex ?: "#3B82F6"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = account?.name ?: "Cuenta",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (account != null) {
                        BounceIconButton(onClick = { onEditAccount(account) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                        }
                        BounceIconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Eliminar", tint = ExpenseRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hero Card con saldo de la cuenta
                item {
                    ObsidianCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = accentColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        // Badge de tipo de cuenta
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = typeLabel(account?.type ?: "CASH"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (account?.isCreditCard == true) "DEUDA ACTUAL" else "SALDO DISPONIBLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (account?.isCreditCard == true) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        MoneyDisplay(
                            amount = if (account?.isCreditCard == true) -balance else balance,
                            currencySymbol = currencySymbol,
                            isIncome = account?.isCreditCard != true,
                            fontSize = 34
                        )

                        if (account?.isCreditCard == true && balance < 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ExpenseRed.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "💳 Para pagar esta deuda, registra una Transferencia desde tu cuenta bancaria hacia esta tarjeta.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }

                // Header transacciones
                item {
                    Text(
                        text = "Movimientos de esta Cuenta",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Lista de transacciones filtradas
                if (uiState.transactions.isEmpty()) {
                    item {
                        ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay movimientos registrados en esta cuenta.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.transactions, key = { it.id }) { tx ->
                        AccountTransactionRow(transaction = tx, currencySymbol = currencySymbol, accountId = accountId)
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteConfirm && account != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Cuenta", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Eliminar \"${account.name}\"? Las transacciones históricas se conservarán pero quedarán sin cuenta asignada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(account.id)
                    showDeleteConfirm = false
                    onNavigateBack()
                }) {
                    Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun AccountTransactionRow(
    transaction: TransactionEntity,
    currencySymbol: String,
    accountId: String
) {
    val isIncome = transaction.type == "INCOME"
    val isTransfer = transaction.type == "TRANSFER"
    // Si es una transferencia que LLEGA a esta cuenta, es positivo
    val isIncomingTransfer = isTransfer && transaction.destinationAccountId == accountId

    ObsidianCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isTransfer -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                isIncome -> IncomeGreen.copy(alpha = 0.15f)
                                else -> ExpenseRed.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isTransfer -> Icons.Outlined.SwapHoriz
                            isIncome -> Icons.Outlined.TrendingUp
                            else -> Icons.Outlined.ReceiptLong
                        },
                        contentDescription = null,
                        tint = when {
                            isTransfer -> MaterialTheme.colorScheme.primary
                            isIncome -> IncomeGreen
                            else -> ExpenseRed
                        },
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            MoneyDisplay(
                amount = transaction.amount,
                currencySymbol = currencySymbol,
                isIncome = isIncome || isIncomingTransfer,
                showSign = true,
                fontSize = 16
            )
        }
    }
}

private fun typeLabel(type: String): String = when (type) {
    "BANK_ACCOUNT" -> "CUENTA BANCARIA"
    "DIGITAL_WALLET" -> "BILLETERA DIGITAL"
    "CREDIT_CARD" -> "TARJETA DE CRÉDITO"
    else -> "EFECTIVO"
}
