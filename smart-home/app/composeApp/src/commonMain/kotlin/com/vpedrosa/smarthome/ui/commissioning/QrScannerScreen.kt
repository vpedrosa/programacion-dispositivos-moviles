package com.vpedrosa.smarthome.ui.commissioning

import androidx.compose.runtime.Composable

@Composable
expect fun QrScannerScreen(
    onQrScanned: (String) -> Unit,
    onNavigateBack: () -> Unit,
)
