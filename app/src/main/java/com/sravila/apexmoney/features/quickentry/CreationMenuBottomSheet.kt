package com.sravila.apexmoney.features.quickentry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationMenuBottomSheet(
    onDismissRequest: () -> Unit,
    onSelectTransaction: () -> Unit,
    onSelectBudget: () -> Unit,
    onSelectVault: () -> Unit,
    onSelectRecurring: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "¿Qué deseas registrar?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CreationMenuItem(
                icon = Icons.Outlined.Add,
                title = "Ingreso o Gasto",
                subtitle = "Registra una transacción rápida",
                onClick = onSelectTransaction
            )
            
            CreationMenuItem(
                icon = Icons.Outlined.PieChart,
                title = "Nuevo Presupuesto",
                subtitle = "Define un límite para una categoría",
                onClick = onSelectBudget
            )

            CreationMenuItem(
                icon = Icons.Outlined.AccountBalance,
                title = "Nueva Bóveda",
                subtitle = "Establece una meta de ahorro",
                onClick = onSelectVault
            )

            CreationMenuItem(
                icon = Icons.Outlined.Repeat,
                title = "Nueva Suscripción",
                subtitle = "Registra un pago recurrente mensual",
                onClick = onSelectRecurring
            )
        }
    }
}

@Composable
fun CreationMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
