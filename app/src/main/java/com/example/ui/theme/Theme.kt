package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IoTDarkColorScheme = darkColorScheme(
    primary = OemSurfaceElevated,
    onPrimary = OemTextWhite,
    primaryContainer = OemSurfaceElevated,
    onPrimaryContainer = OemTextColdWhite,
    secondary = OemWarningAmber,
    onSecondary = Color.Black,
    tertiary = OemOptimalGreen,
    onTertiary = Color.Black,
    background = OemCanvasBackground,
    onBackground = OemTextColdWhite,
    surface = OemCardSurface,
    onSurface = OemTextColdWhite,
    surfaceVariant = OemSurfaceElevated,
    onSurfaceVariant = OemTextSecondary,
    outline = OemBorderSubtle,
    outlineVariant = OemBorderSubtle.copy(alpha = 0.5f),
    error = OemCriticalRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IoTDarkColorScheme,
        typography = Typography,
        content = content
    )
}
