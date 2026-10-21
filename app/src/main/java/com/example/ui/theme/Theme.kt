package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IoTDarkColorScheme = darkColorScheme(
    primary = IoTCyanAccent,
    onPrimary = Color(0xFF002A30),
    primaryContainer = Color(0xFF004E59),
    onPrimaryContainer = IoTCyanBright,
    secondary = IoTGreenForward,
    onSecondary = Color.White,
    tertiary = IoTBlueReverse,
    onTertiary = Color.White,
    background = IoTBackgroundDark,
    onBackground = IoTTextPrimary,
    surface = IoTSurfaceCard,
    onSurface = IoTTextPrimary,
    surfaceVariant = IoTSurfaceElevated,
    onSurfaceVariant = IoTTextSecondary,
    error = IoTRedStop,
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
