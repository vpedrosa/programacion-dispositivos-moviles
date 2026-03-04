package com.vpedrosa.smarthome.wear.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

// Matching the main app's color palette
val Navy = Color(0xFF123458)
val Linen = Color(0xFFF1EFEC)
val WarmGray = Color(0xFFD4C9BE)
val Black = Color(0xFF030303)

val NavyLight = Color(0xFF1E4D7B)
val NavyDark = Color(0xFF0D2440)
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFE57373)

private val SmartHomeWearColorScheme = ColorScheme(
    primary = Navy,
    onPrimary = Linen,
    primaryContainer = NavyLight,
    onPrimaryContainer = Linen,
    secondary = WarmGray,
    onSecondary = Black,
    secondaryContainer = WarmGray,
    onSecondaryContainer = Black,
    background = Black,
    onBackground = Linen,
    onSurface = Linen,
    onSurfaceVariant = WarmGray,
    error = ErrorRed,
    onError = Black,
)

@Composable
fun SmartHomeWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SmartHomeWearColorScheme,
        content = content,
    )
}
