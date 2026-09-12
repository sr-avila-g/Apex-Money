package com.sravila.apexmoney.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.sravila.apexmoney.core.ui.bounceClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sravila.apexmoney.core.theme.*
import com.sravila.apexmoney.core.ui.ObsidianCard
import com.sravila.apexmoney.core.ui.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userPrefs by viewModel.userPreferences.collectAsState()
    val activeTheme = userPrefs?.themeMode ?: ThemeMode.AMOLED
    val activeCurrencyCode = userPrefs?.currencyCode ?: "USD"
    val activeCurrencySymbol = userPrefs?.currencySymbol ?: "$"

    val currencies = listOf(
        Pair("$", "USD"),
        Pair("$", "COP"),
        Pair("$", "MXN"),
        Pair("€", "EUR"),
        Pair("£", "GBP"),
        Pair("Bs", "BOB")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. Seguridad y Privacidad
            item {
                SectionHeader(title = "Seguridad y Privacidad")
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    SettingToggleRow(
                        title = "Modo Discreto",
                        subtitle = "Oculta saldos con puntos (• • • •)",
                        icon = Icons.Outlined.VisibilityOff,
                        isChecked = userPrefs?.isDiscreetMode ?: false,
                        onCheckedChange = { viewModel.togglePreference("discreet", userPrefs?.isDiscreetMode ?: false) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    SettingToggleRow(
                        title = "App Lock Biométrica",
                        subtitle = "Protege con Huella o Face ID",
                        icon = Icons.Outlined.Fingerprint,
                        isChecked = userPrefs?.isBiometricEnabled ?: false,
                        onCheckedChange = { viewModel.togglePreference("biometric", userPrefs?.isBiometricEnabled ?: false) }
                    )
                }
            }
            
            // 2. Finanzas y Formato
            item {
                SectionHeader(title = "Finanzas y Formato")
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Divisa Principal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { (symbol, code) ->
                            val isSelected = (activeCurrencyCode == code)
                            val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                            Box(
                                modifier = Modifier
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(100))
                                    .background(chipBg)
                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(100))
                                    .clickable { viewModel.setCurrency(symbol, code) }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (code.isEmpty()) symbol else "$symbol $code",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = chipText
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    SettingToggleRow(
                        title = "Presupuestos Estrictos",
                        subtitle = "Alerta roja al exceder límites",
                        icon = Icons.Outlined.WarningAmber,
                        isChecked = userPrefs?.isStrictBudget ?: false,
                        onCheckedChange = { viewModel.togglePreference("strict_budget", userPrefs?.isStrictBudget ?: false) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    SettingToggleRow(
                        title = "Redondeo Automático",
                        subtitle = "Envía centavos a bóvedas (Micro-Ahorro)",
                        icon = Icons.Outlined.Savings,
                        isChecked = userPrefs?.isRoundUpEnabled ?: false,
                        onCheckedChange = { viewModel.togglePreference("round_up", userPrefs?.isRoundUpEnabled ?: false) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    SettingToggleRow(
                        title = "Reverso Automático",
                        subtitle = "Reembolso al desmarcar suscripciones",
                        icon = Icons.Outlined.Autorenew,
                        isChecked = userPrefs?.isAutoReversalEnabled ?: true,
                        onCheckedChange = { viewModel.togglePreference("auto_reversal", userPrefs?.isAutoReversalEnabled ?: true) }
                    )
                }
            }

            // 3. Experiencia y Temas
            item {
                SectionHeader(title = "Experiencia Visual")
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    SettingToggleRow(
                        title = "Respuesta Háptica",
                        subtitle = "Vibración al interactuar",
                        icon = Icons.Outlined.Vibration,
                        isChecked = userPrefs?.isHapticEnabled ?: true,
                        onCheckedChange = { viewModel.togglePreference("haptic", userPrefs?.isHapticEnabled ?: true) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    Text(
                        text = "Temas de Interfaz",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ThemeOptionRow(
                        title = "Modo Amoled",
                        subtitle = "Negro Puro & Neón",
                        accentColor = NeonPrimary,
                        isSelected = activeTheme == ThemeMode.AMOLED,
                        onSelect = { viewModel.setThemeMode(ThemeMode.AMOLED) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ThemeOptionRow(
                        title = "Soft Day",
                        subtitle = "Beige & Lila",
                        accentColor = SoftPrimary,
                        isSelected = activeTheme == ThemeMode.SOFT_DAY,
                        onSelect = { viewModel.setThemeMode(ThemeMode.SOFT_DAY) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemeOptionRow(
                        title = "Ocean Depths",
                        subtitle = "Azul Submarino & Plata",
                        accentColor = OceanPrimary,
                        isSelected = activeTheme == ThemeMode.OCEAN_DEPTHS,
                        onSelect = { viewModel.setThemeMode(ThemeMode.OCEAN_DEPTHS) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemeOptionRow(
                        title = "Blood Sun",
                        subtitle = "Rojo Intenso & Amarillo",
                        accentColor = BloodPrimary,
                        isSelected = activeTheme == ThemeMode.BLOOD_SUN,
                        onSelect = { viewModel.setThemeMode(ThemeMode.BLOOD_SUN) }
                    )
                }
            }


            // Sincronización en la Nube
            item {
                SectionHeader(title = "Nube y Sincronización")
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    val rawDate = userPrefs?.lastSyncTimestamp ?: "1970-01-01T00:00:00Z"
                    val lastSyncText = if (rawDate.startsWith("1970")) {
                        "Nunca"
                    } else {
                        try {
                            val instant = java.time.Instant.parse(rawDate)
                            val local = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM, HH:mm")
                            local.format(formatter)
                        } catch(e: Exception) {
                            "Desconocida"
                        }
                    }

                    ActionRow(
                        title = "Sincronizar Ahora (Supabase)",
                        subtitle = "Última sincronización: $lastSyncText",
                        icon = androidx.compose.material.icons.Icons.Outlined.Sync,
                        onClick = { 
                            com.sravila.apexmoney.core.network.SyncManager.triggerManualSync(context)
                            android.widget.Toast.makeText(context, "Sincronización iniciada en segundo plano...", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }


            // Papelera de Reciclaje
            item {
                SectionHeader(title = "Papelera de Reciclaje")
                val trashCount by viewModel.trashCount.collectAsState()
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    ActionRow(
                        title = "Vaciar Papelera Ahora",
                        subtitle = if (trashCount == 0) "La papelera está vacía" else "Eliminar definitivamente $trashCount elementos ocultos",
                        icon = androidx.compose.material.icons.Icons.Outlined.DeleteForever,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = {
                            if (trashCount > 0) {
                                viewModel.emptyTrash { success, message ->
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                android.widget.Toast.makeText(context, "La papelera ya está vacía", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Outlined.AutoDelete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Limpieza Automática",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Se borrarán registros ocultos con más de 30 días",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. Datos y Respaldo
            item {
                SectionHeader(title = "Datos Locales")
                ObsidianCard(modifier = Modifier.fillMaxWidth()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    ActionRow(
                        title = "Exportar Datos (Backup)",
                        subtitle = "Guarda tu historial en CSV/JSON",
                        icon = Icons.Outlined.Download,
                        onClick = { viewModel.exportData(context) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ActionRow(
                        title = "Importar Datos",
                        subtitle = "Restaura desde un archivo",
                        icon = Icons.Outlined.UploadFile,
                        onClick = { viewModel.importData(context) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ActionRow(
                        title = "Borrar Todo (Wipe Data)",
                        subtitle = "Elimina permanentemente todo el historial",
                        icon = Icons.Outlined.DeleteForever,
                        iconTint = ExpenseRed,
                        onClick = { viewModel.wipeData { } }
                    )
                }
            }

            // 5. Información del Sistema & Firma Sr. Avila
            item {
                SectionHeader(title = "Acerca de Apex Money")
                ObsidianCard(
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Apex Money",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Versión 2.0.0 Eilish Release",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Próximas Actualizaciones:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Sincronización en la Nube (Supabase)\n• Widgets de Escritorio\n• Modo Familiar Compartido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Cita Célebre
                    Row {
                        Icon(
                            imageVector = Icons.Outlined.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\"La riqueza no consiste en tener grandes posesiones, sino en tener pocas necesidades.\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "— Epícteto",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Desarrollado con excelencia por Sr. Avila",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)).clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = null // handled by Row click
        )
    }
}

@Composable
fun ActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)).clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ThemeOptionRow(
    title: String,
    subtitle: String,
    accentColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp)).clickable { onSelect() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Seleccionado",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
