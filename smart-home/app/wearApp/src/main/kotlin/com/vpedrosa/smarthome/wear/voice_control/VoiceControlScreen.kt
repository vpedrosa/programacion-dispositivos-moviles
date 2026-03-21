package com.vpedrosa.smarthome.wear.voice_control

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.vpedrosa.smarthome.wear.R
import com.vpedrosa.smarthome.wear.theme.ErrorRed
import com.vpedrosa.smarthome.wear.theme.Linen
import com.vpedrosa.smarthome.wear.theme.Navy
import com.vpedrosa.smarthome.wear.theme.SuccessGreen

@Composable
fun VoiceControlScreen(viewModel: VoiceControlViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull()
            if (text != null) {
                viewModel.onSpeechResult(text)
            } else {
                viewModel.onSpeechError(context.getString(R.string.speech_no_text_recognized))
            }
        }
        // RESULT_CANCELED → user backed out, stay Idle
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
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                        putExtra(
                            RecognizerIntent.EXTRA_PROMPT,
                            context.getString(R.string.voice_speech_prompt),
                        )
                    }
                    try {
                        speechLauncher.launch(intent)
                    } catch (_: Exception) {
                        viewModel.onSpeechError(
                            context.getString(R.string.speech_not_available),
                        )
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
    val buttonColor = when (status) {
        VoiceStatus.Idle -> Navy
        VoiceStatus.Processing -> Navy
        VoiceStatus.Result -> SuccessGreen
        VoiceStatus.Error -> ErrorRed
    }

    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        enabled = status != VoiceStatus.Processing,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = buttonColor,
            contentColor = Linen,
            disabledContainerColor = buttonColor.copy(alpha = 0.8f),
            disabledContentColor = Linen.copy(alpha = 0.8f),
        ),
    ) {
        androidx.wear.compose.material3.Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = stringResource(R.string.a11y_microphone),
            modifier = Modifier.size(32.dp),
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
                        text = stringResource(R.string.voice_tap_to_speak),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                VoiceStatus.Processing -> {
                    Text(
                        text = stringResource(R.string.voice_processing),
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
