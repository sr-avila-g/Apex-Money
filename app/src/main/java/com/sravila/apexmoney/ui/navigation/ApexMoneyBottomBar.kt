package com.sravila.apexmoney.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.material.icons.outlined.*

sealed class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Dashboard : NavItem("dashboard", "Mando", Icons.Outlined.Dashboard, Icons.Filled.Dashboard)
    object Budgets : NavItem("budgets", "Presupuesto", Icons.Outlined.PieChart, Icons.Filled.PieChart)
    object Vaults : NavItem("vaults", "Bóvedas", Icons.Outlined.AccountBalance, Icons.Filled.AccountBalance)
    object Analytics : NavItem("analytics", "Informes", Icons.Outlined.Insights, Icons.Filled.Insights)
    object Recurring : NavItem("recurring", "Radar", Icons.Outlined.Repeat, Icons.Filled.Repeat)
    object Settings : NavItem("settings", "Ajustes", Icons.Outlined.Settings, Icons.Filled.Settings)
    object AccountDetail : NavItem("account_detail/{accountId}", "Cuenta", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet) {
        fun createRoute(accountId: String) = "account_detail/$accountId"
    }
}

@Composable
fun ApexMoneyBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onQuickEntryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem.Dashboard,
        NavItem.Budgets,
        NavItem.Vaults,
        NavItem.Recurring,
        NavItem.Analytics
    )

    // Translucent floating style
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val interactionSource = remember { MutableInteractionSource() }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onNavigate(item.route) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(56.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
