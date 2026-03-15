package com.vpedrosa.smarthome.wear.voice_control

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.vpedrosa.smarthome.wear.theme.ErrorRed
import com.vpedrosa.smarthome.wear.theme.Linen
import com.vpedrosa.smarthome.wear.theme.Navy
import com.vpedrosa.smarthome.wear.theme.SuccessGreen

@Composable
fun VoiceControlScreen(viewModel: VoiceControlViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            viewModel.onMicPressed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MicButton(
                status = uiState.status,
                onClick = {
                    if (hasAudioPermission) {
                        viewModel.onMicPressed()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusText(uiState = uiState)
        }
    }
}

@Composable
private fun MicButton(
    status: VoiceStatus,
    onClick: () -> Unit,
) {
    val isActive = status == VoiceStatus.Listening

    // Pulsing animation when listening
    val scale = if (isActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val animatedScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "micPulse",
        )
        animatedScale
    } else {
        1f
    }

    val buttonColor = when (status) {
        VoiceStatus.Idle -> Navy
        VoiceStatus.Listening -> Color(0xFF1E4D7B)
        VoiceStatus.Processing -> Navy
        VoiceStatus.Result -> SuccessGreen
        VoiceStatus.Error -> ErrorRed
    }

    FilledIconButton(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp)
            .scale(scale),
        enabled = status == VoiceStatus.Idle
                || status == VoiceStatus.Listening
                || status == VoiceStatus.Result
                || status == VoiceStatus.Error,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = buttonColor,
            contentColor = Linen,
            disabledContainerColor = buttonColor.copy(alpha = 0.8f),
            disabledContentColor = Linen.copy(alpha = 0.8f),
        ),
    ) {
        Text(
            text = "\uD83C\uDF99",  // Microphone emoji as icon placeholder
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun StatusText(uiState: VoiceControlUiState) {
    AnimatedContent(
        targetState = uiState.status,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "statusTransition",
    ) { status ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (status) {
                VoiceStatus.Idle -> {
                    Text(
                        text = "Toca para hablar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                VoiceStatus.Listening -> {
                    Text(
                        text = "Escuchando...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                VoiceStatus.Processing -> {
                    Text(
                        text = "Procesando...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (uiState.transcribedText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${uiState.transcribedText}\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                VoiceStatus.Result -> {
                    Text(
                        text = uiState.resultMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        textAlign = TextAlign.Center,
                    )
                }
                VoiceStatus.Error -> {
                    Text(
                        text = uiState.resultMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
