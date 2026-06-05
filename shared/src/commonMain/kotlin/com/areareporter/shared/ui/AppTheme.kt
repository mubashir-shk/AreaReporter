package com.areareporter.shared.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colors
private val PrimaryBlue = Color(0xFF1565C0)
private val PrimaryBlueDark = Color(0xFF90CAF9)
private val ErrorRed = Color(0xFFB71C1C)
private val WarningAmber = Color(0xFFFF8F00)
private val SuccessGreen = Color(0xFF2E7D32)
private val SurfaceDark = Color(0xFF1A1A2E)
private val BackgroundDark = Color(0xFF0F0F1E)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F7),
    background = Color(0xFFF8F9FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFDEE3EB),
    onSurface = Color(0xFF1A1C1E),
    error = ErrorRed,
    outline = Color(0xFF73777F)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF004888),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF43474E),
    onSurface = Color(0xFFE2E2E9),
    error = Color(0xFFFFB4AB),
    outline = Color(0xFF8D9199)
)

/**
 * App theme with Material 3 and dark mode support.
 */
@Composable
fun AreaReporterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Status color helpers used across UI
object StatusColors {
    val pending = WarningAmber
    val syncing = Color(0xFF1565C0)
    val synced = SuccessGreen
    val failed = ErrorRed
}
