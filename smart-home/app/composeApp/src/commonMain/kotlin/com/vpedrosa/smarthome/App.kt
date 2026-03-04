package com.vpedrosa.smarthome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val Linen = Color(0xFFF1EFEC)
private val WarmGray = Color(0xFFD4C9BE)
private val Navy = Color(0xFF123458)
private val Black = Color(0xFF030303)

private val SmartHomeColorScheme = darkColorScheme(
    primary = Navy,
    secondary = WarmGray,
    background = Linen,
    surface = Linen,
    onPrimary = Linen,
    onSecondary = Black,
    onBackground = Black,
    onSurface = Black,
)

@Composable
fun App() {
    MaterialTheme(colorScheme = SmartHomeColorScheme) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Smart Home",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
