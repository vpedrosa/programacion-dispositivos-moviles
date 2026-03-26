package com.vpedrosa.smarthome.ui.voice

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommand
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommandResult
import com.vpedrosa.smarthome.ui.components.AudioPermissionHandler
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.title_voice_control
import smarthome.composeapp.generated.resources.voice_hint
import smarthome.composeapp.generated.resources.voice_listening
import smarthome.composeapp.generated.resources.voice_recent_commands
import smarthome.composeapp.generated.resources.voice_tap_to_speak
import smarthome.composeapp.generated.resources.voice_no_recent
import smarthome.composeapp.generated.resources.voice_devices_affected
import smarthome.composeapp.generated.resources.voice_stop_listening
import smarthome.composeapp.generated.resources.voice_start_listening
import smarthome.composeapp.generated.resources.action_back
import smarthome.composeapp.generated.resources.a11y_command_success
import smarthome.composeapp.generated.resources.a11y_command_error
import com.vpedrosa.smarthome.ui.theme.Navy
import com.vpedrosa.smarthome.ui.theme.Linen
import com.vpedrosa.smarthome.ui.theme.WarmGray
import com.vpedrosa.smarthome.ui.theme.Black
import com.vpedrosa.smarthome.ui.theme.SuccessGreen
import com.vpedrosa.smarthome.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceControlScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: VoiceControlViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.title_voice_control),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.action_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Linen,
                    titleContentColor = Black,
                    navigationIconContentColor = Black,
                ),
            )
        },
        containerColor = Linen,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Microphone button with audio permission handling
            AudioPermissionHandler(
                onPermissionResult = { granted ->
                    if (granted && !state.isListening) {
                        viewModel.toggleListening()
                    }
                },
            ) { hasPermission, requestPermission ->
                MicrophoneButton(
                    isListening = state.isListening,
                    onClick = {
                        if (hasPermission) {
                            viewModel.toggleListening()
                        } else {
                            requestPermission()
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status text
            StatusText(
                isListening = state.isListening,
                recognizedText = state.recognizedText,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (state.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                    color = Navy,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Result card
            if (state.lastResult != null) {
                ResultCard(result = state.lastResult!!)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent commands section
            if (state.recentCommands.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.voice_recent_commands),
                    style = MaterialTheme.typography.labelLarge,
                    color = Black.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.recentCommands) { command ->
                    RecentCommandItem(command = command)
                }

                if (state.recentCommands.isEmpty() && state.lastResult == null) {
                    item {
                        Text(
                            text = stringResource(Res.string.voice_no_recent),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Black.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MicrophoneButton(
    isListening: Boolean,
    onClick: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )

    val bgColor by animateColorAsState(
        targetValue = if (isListening) ErrorRed else Navy,
        label = "micBg",
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(if (isListening) scale else 1f)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
            contentDescription = if (isListening) stringResource(Res.string.voice_stop_listening) else stringResource(Res.string.voice_start_listening),
            tint = Color.White,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun StatusText(
    isListening: Boolean,
    recognizedText: String,
) {
    when {
        isListening -> {
            Text(
                text = stringResource(Res.string.voice_listening),
                style = MaterialTheme.typography.titleMedium,
                color = Navy,
                fontWeight = FontWeight.SemiBold,
            )
        }
        recognizedText.isNotBlank() -> {
            Text(
                text = "\"$recognizedText\"",
                style = MaterialTheme.typography.bodyLarge,
                color = Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        else -> {
            Text(
                text = stringResource(Res.string.voice_tap_to_speak),
                style = MaterialTheme.typography.bodyMedium,
                color = Black.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.voice_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Black.copy(alpha = 0.35f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ResultCard(result: VoiceCommandResult) {
    val cardColor = if (result.success) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
    val iconColor = if (result.success) SuccessGreen else ErrorRed
    val icon = if (result.success) Icons.Filled.CheckCircle else Icons.Filled.Error
    val iconDesc = if (result.success) Res.string.a11y_command_success else Res.string.a11y_command_error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(iconDesc),
                tint = iconColor,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Black,
                )
                if (result.devicesAffected > 0) {
                    Text(
                        text = stringResource(
                            Res.string.voice_devices_affected,
                            result.devicesAffected,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Black.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentCommandItem(command: VoiceCommand) {
    val iconColor = if (command.result.success) SuccessGreen else ErrorRed
    val icon = if (command.result.success) Icons.Filled.CheckCircle else Icons.Filled.Error
    val iconDesc = if (command.result.success) Res.string.a11y_command_success else Res.string.a11y_command_error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(iconDesc),
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = command.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = command.result.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Black.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
