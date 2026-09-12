package com.sravila.apexmoney.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    NEON_NIGHT,
    SOFT_DAY,
    OCEAN_DEPTHS,
    BLOOD_SUN,
    STANDARD_LIGHT,
    STANDARD_DARK,
    AMOLED
}

// 1. NEON NIGHT (Dark)
private val NeonNightScheme = darkColorScheme(
    background = NeonBackground,
    surface = NeonSurface,
    surfaceVariant = NeonSurface,
    primary = NeonPrimary,
    onPrimary = NeonOnPrimary,
    onBackground = NeonTextPrimary,
    onSurface = NeonTextPrimary,
    onSurfaceVariant = NeonTextSecondary,
    outline = NeonOutline
)

// 2. SOFT DAY (Light)
private val SoftDayScheme = lightColorScheme(
    background = SoftBackground,
    surface = SoftSurface,
    surfaceVariant = SoftSurface,
    primary = SoftPrimary,
    onPrimary = SoftOnPrimary,
    onBackground = SoftTextPrimary,
    onSurface = SoftTextPrimary,
    onSurfaceVariant = SoftTextSecondary,
    outline = SoftOutline
)

// 3. OCEAN DEPTHS (Dark)
private val OceanDepthsScheme = darkColorScheme(
    background = OceanBackground,
    surface = OceanSurface,
    surfaceVariant = OceanSurface,
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    onBackground = OceanTextPrimary,
    onSurface = OceanTextPrimary,
    onSurfaceVariant = OceanTextSecondary,
    outline = OceanOutline
)

// 4. BLOOD SUN (Dark)
private val BloodSunScheme = darkColorScheme(
    background = BloodBackground,
    surface = BloodSurface,
    surfaceVariant = BloodSurface,
    primary = BloodPrimary,
    onPrimary = BloodOnPrimary,
    onBackground = BloodTextPrimary,
    onSurface = BloodTextPrimary,
    onSurfaceVariant = BloodTextSecondary,
    outline = BloodOutline
)

// 5. STANDARD LIGHT
private val StandardLightScheme = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurface,
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline
)

// 6. STANDARD DARK
private val StandardDarkScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurface,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline
)

// 7. AMOLED
private val AmoledScheme = darkColorScheme(
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurface,
    primary = AmoledPrimary,
    onPrimary = AmoledOnPrimary,
    onBackground = AmoledTextPrimary,
    onSurface = AmoledTextPrimary,
    onSurfaceVariant = AmoledTextSecondary,
    outline = AmoledOutline
)

@Composable
fun ApexMoneyTheme(
    themeMode: ThemeMode = ThemeMode.AMOLED,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.NEON_NIGHT -> NeonNightScheme
        ThemeMode.SOFT_DAY -> SoftDayScheme
        ThemeMode.OCEAN_DEPTHS -> OceanDepthsScheme
        ThemeMode.BLOOD_SUN -> BloodSunScheme
        ThemeMode.STANDARD_LIGHT -> StandardLightScheme
        ThemeMode.STANDARD_DARK -> StandardDarkScheme
        ThemeMode.AMOLED -> AmoledScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Enforce window to draw system bar backgrounds
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)

            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            // Set light status bar icons for light themes
            val isLight = themeMode == ThemeMode.SOFT_DAY || themeMode == ThemeMode.STANDARD_LIGHT
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ApexTypography,
        content = content
    )
}
