package com.vpedrosa.smarthome.ui.antisquatter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.anti_squatter_active_subtitle
import smarthome.composeapp.generated.resources.anti_squatter_inactive_subtitle
import smarthome.composeapp.generated.resources.anti_squatter_presence_simulation
import smarthome.composeapp.generated.resources.anti_squatter_schedule_section
import smarthome.composeapp.generated.resources.anti_squatter_start_time
import smarthome.composeapp.generated.resources.anti_squatter_end_time
import smarthome.composeapp.generated.resources.anti_squatter_action_duration
import smarthome.composeapp.generated.resources.anti_squatter_duration_minutes
import smarthome.composeapp.generated.resources.anti_squatter_summary_actions
import smarthome.composeapp.generated.resources.anti_squatter_validation_error
import smarthome.composeapp.generated.resources.anti_squatter_description
import smarthome.composeapp.generated.resources.a11y_navigate_back
import smarthome.composeapp.generated.resources.title_anti_squatter
import com.vpedrosa.smarthome.ui.theme.ActiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntiSquatterScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AntiSquatterViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.title_anti_squatter),
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.a11y_navigate_back),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Master toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isEnabled) ActiveGreen.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.anti_squatter_presence_simulation),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                if (state.isEnabled) Res.string.anti_squatter_active_subtitle
                                else Res.string.anti_squatter_inactive_subtitle,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        )
                    }
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = { viewModel.toggleEnabled() },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = ActiveGreen,
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                            uncheckedThumbColor = Color.White,
                        ),
                    )
                }
            }

            // Description
            Text(
                text = stringResource(Res.string.anti_squatter_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )

            // Schedule section
            Text(
                text = stringResource(Res.string.anti_squatter_schedule_section),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Start time
                    TimeInputRow(
                        label = stringResource(Res.string.anti_squatter_start_time),
                        hour = state.startHour,
                        minute = state.startMinute,
                        onTimeChange = viewModel::updateStartTime,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // End time
                    TimeInputRow(
                        label = stringResource(Res.string.anti_squatter_end_time),
                        hour = state.endHour,
                        minute = state.endMinute,
                        onTimeChange = viewModel::updateEndTime,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(Res.string.anti_squatter_action_duration),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = state.actionDurationMinutes.toString(),
                                onValueChange = { text ->
                                    text.toIntOrNull()?.let { viewModel.updateActionDuration(it) }
                                },
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.anti_squatter_duration_minutes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }

            // Validation error
            if (!state.isValid) {
                Text(
                    text = stringResource(Res.string.anti_squatter_validation_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // Summary
            if (state.isValid && state.maxActions > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    ),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(
                                Res.string.anti_squatter_summary_actions,
                                state.maxActions,
                                state.actionDurationMinutes,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeInputRow(
    label: String,
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = hour.toString().padStart(2, '0'),
                onValueChange = { text ->
                    text.toIntOrNull()?.coerceIn(0, 23)?.let { onTimeChange(it, minute) }
                },
                modifier = Modifier.width(64.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            OutlinedTextField(
                value = minute.toString().padStart(2, '0'),
                onValueChange = { text ->
                    text.toIntOrNull()?.coerceIn(0, 59)?.let { onTimeChange(hour, it) }
                },
                modifier = Modifier.width(64.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}
