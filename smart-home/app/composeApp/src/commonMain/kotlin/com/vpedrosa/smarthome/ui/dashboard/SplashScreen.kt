package com.vpedrosa.smarthome.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.app_name
import smarthome.composeapp.generated.resources.logotipo

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.logotipo),
            contentDescription = stringResource(Res.string.app_name),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
        )
    }
}
