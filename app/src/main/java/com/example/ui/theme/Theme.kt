package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProDarkColorScheme = darkColorScheme(
    primary = MeterAmber,
    onPrimary = Color(0xFF1E1705),
    primaryContainer = MeterAmberContainer,
    onPrimaryContainer = MeterAmberLight,
    secondary = MeterCyan,
    onSecondary = Color(0xFF03283E),
    tertiary = MeterGreen,
    onTertiary = Color(0xFF062B1C),
    background = ProDarkBackground,
    onBackground = ProDarkTextPrimary,
    surface = ProDarkSurface,
    onSurface = ProDarkTextPrimary,
    surfaceVariant = ProDarkSurfaceVariant,
    onSurfaceVariant = ProDarkTextSecondary,
    outline = ProDarkBorder,
    error = MeterRed,
    onError = Color.White
)

private val AstroRedColorScheme = darkColorScheme(
    primary = AstroRedPrimary,
    onPrimary = Color.Black,
    primaryContainer = AstroRedSurfaceVariant,
    onPrimaryContainer = AstroRedText,
    secondary = AstroRedSecondary,
    onSecondary = Color.Black,
    tertiary = AstroRedPrimary,
    onTertiary = Color.Black,
    background = AstroBlack,
    onBackground = AstroRedText,
    surface = AstroRedSurface,
    onSurface = AstroRedText,
    surfaceVariant = AstroRedSurfaceVariant,
    onSurfaceVariant = AstroRedTextMuted,
    outline = AstroRedBorder,
    error = AstroRedPrimary,
    onError = Color.Black
)

private val BatterySaverColorScheme = darkColorScheme(
    primary = MeterAmberDark,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1A150D),
    onPrimaryContainer = MeterAmber,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.Black,
    tertiary = Color(0xFF059669),
    onTertiary = Color.Black,
    background = Color(0xFF000000), // Pure OLED black
    onBackground = Color(0xFFCBD5E1),
    surface = Color(0xFF050505),
    onSurface = Color(0xFFCBD5E1),
    surfaceVariant = Color(0xFF0D0D0D),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFF1E293B),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun LightmeterProTheme(
    isRedNightMode: Boolean = false,
    isBatterySaverMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isRedNightMode -> AstroRedColorScheme
        isBatterySaverMode -> BatterySaverColorScheme
        else -> ProDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
