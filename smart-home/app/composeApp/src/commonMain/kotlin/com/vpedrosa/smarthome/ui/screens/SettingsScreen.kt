package com.vpedrosa.smarthome.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.settings_about
import smarthome.composeapp.generated.resources.settings_about_value
import smarthome.composeapp.generated.resources.settings_devices
import smarthome.composeapp.generated.resources.settings_door_alerts
import smarthome.composeapp.generated.resources.settings_door_alerts_subtitle
import smarthome.composeapp.generated.resources.settings_general
import smarthome.composeapp.generated.resources.settings_language
import smarthome.composeapp.generated.resources.settings_language_value
import smarthome.composeapp.generated.resources.settings_manage_simulated
import smarthome.composeapp.generated.resources.settings_matter_commissioning
import smarthome.composeapp.generated.resources.settings_modes
import smarthome.composeapp.generated.resources.settings_notifications_section
import smarthome.composeapp.generated.resources.settings_sensor_alerts
import smarthome.composeapp.generated.resources.settings_thermostat_events
import smarthome.composeapp.generated.resources.settings_thermostat_events_subtitle
import smarthome.composeapp.generated.resources.title_anti_squatter
import smarthome.composeapp.generated.resources.title_settings

private val ActiveGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAntiSquatter: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
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
                    text = stringResource(Res.string.title_settings),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // --- DISPOSITIVOS ---
            SectionHeader(text = stringResource(Res.string.settings_devices))
            Spacer(modifier = Modifier.height(8.dp))
            SettingsNavigationItem(
                title = stringResource(Res.string.settings_matter_commissioning),
                subtitle = stringResource(Res.string.settings_manage_simulated),
                onClick = { /* Matter commissioning - not implemented */ },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- NOTIFICACIONES ---
            SectionHeader(text = stringResource(Res.string.settings_notifications_section))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                ),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    SettingsToggleItem(
                        title = stringResource(Res.string.settings_sensor_alerts),
                        isChecked = state.sensorAlertsEnabled,
                        onToggle = { viewModel.toggleSensorAlerts() },
                    )
                    SettingsToggleItem(
                        title = stringResource(Res.string.settings_door_alerts),
                        subtitle = stringResource(Res.string.settings_door_alerts_subtitle),
                        isChecked = state.doorAlertEnabled,
                        onToggle = { viewModel.toggleDoorAlert() },
                    )
                    SettingsToggleItem(
                        title = stringResource(Res.string.settings_thermostat_events),
                        subtitle = stringResource(Res.string.settings_thermostat_events_subtitle),
                        isChecked = state.thermostatEventsEnabled,
                        onToggle = { viewModel.toggleThermostatEvents() },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- MODOS ---
            SectionHeader(text = stringResource(Res.string.settings_modes))
            Spacer(modifier = Modifier.height(8.dp))
            SettingsNavigationItem(
                title = stringResource(Res.string.title_anti_squatter),
                onClick = onNavigateToAntiSquatter,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- GENERAL ---
            SectionHeader(text = stringResource(Res.string.settings_general))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                ),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    SettingsInfoItem(
                        title = stringResource(Res.string.settings_language),
                        value = stringResource(Res.string.settings_language_value),
                    )
                    SettingsInfoItem(
                        title = stringResource(Res.string.settings_about),
                        value = stringResource(Res.string.settings_about_value),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    )
}

@Composable
private fun SettingsNavigationItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ActiveGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
            ),
        )
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}
