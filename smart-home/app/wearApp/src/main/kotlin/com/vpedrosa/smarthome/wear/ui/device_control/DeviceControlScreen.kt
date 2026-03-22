package com.vpedrosa.smarthome.wear.ui.device_control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blinds
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.vpedrosa.smarthome.wear.R
import com.vpedrosa.smarthome.wear.device_control.domain.model.WearDevice
import com.vpedrosa.smarthome.wear.theme.ErrorRed
import com.vpedrosa.smarthome.wear.theme.SuccessGreen
import com.vpedrosa.smarthome.wear.theme.WarmGray

@Composable
fun DeviceControlScreen(viewModel: DeviceControlViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Title
        item {
            ListHeader {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }

        if (uiState.isLoading) {
            item {
                Text(
                    text = stringResource(R.string.device_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGray,
                )
            }
        }

        if (uiState.error != null) {
            item {
                Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (uiState.devicesByRoom.isEmpty() && !uiState.isLoading) {
            item {
                Text(
                    text = stringResource(R.string.device_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGray,
                    textAlign = TextAlign.Center,
                )
            }
        }

        for ((room, devices) in uiState.devicesByRoom) {
            // Room header
            item {
                ListHeader {
                    Text(
                        text = if (room == DeviceControlViewModel.UNASSIGNED_ROOM) {
                            stringResource(R.string.device_no_room)
                        } else {
                            room
                        },
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            // Devices in this room
            items(devices, key = { it.id }) { device ->
                DeviceItem(
                    device = device,
                    onToggle = { viewModel.toggleDevice(device.id) },
                )
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun DeviceItem(
    device: WearDevice,
    onToggle: () -> Unit,
) {
    val isActionable = device.type in ACTIONABLE_TYPES
    val isActive = device.isDeviceActive()
    val statusColor = if (isActive) SuccessGreen else WarmGray

    FilledTonalButton(
        onClick = onToggle,
        enabled = isActionable,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                androidx.wear.compose.material3.Icon(
                    imageVector = device.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = statusColor,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = device.statusLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
            )
        }
    }
}

private fun WearDevice.icon(): ImageVector = when (type) {
    "LIGHT" -> Icons.Filled.Lightbulb
    "LOCK" -> if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen
    "BLIND" -> Icons.Filled.Blinds
    "SWITCH" -> Icons.Filled.PowerSettingsNew
    "THERMOSTAT" -> Icons.Filled.Thermostat
    "SMART_TV" -> Icons.Filled.Tv
    "SMOKE_SENSOR", "WATER_LEAK_SENSOR", "TEMPERATURE_SENSOR", "CONTACT_SENSOR" -> Icons.Filled.Sensors
    else -> Icons.Filled.DeviceThermostat
}

private fun WearDevice.statusLabel(): String = when (type) {
    "LIGHT", "SWITCH", "SMART_TV" -> if (isOn) "ON" else "OFF"
    "LOCK" -> ""
    "BLIND" -> "${openingLevel}%"
    "THERMOSTAT" -> "${currentTemperature.toInt()}°"
    "SMOKE_SENSOR" -> if (isSmokeDetected) "!!" else "OK"
    "WATER_LEAK_SENSOR" -> if (isLeakDetected) "!!" else "OK"
    "TEMPERATURE_SENSOR" -> "${currentTemperature.toInt()}°"
    "CONTACT_SENSOR" -> if (isContactOpen) "Open" else "Closed"
    else -> ""
}

private fun WearDevice.isDeviceActive(): Boolean = when (type) {
    "LIGHT", "SWITCH", "SMART_TV" -> isOn
    "LOCK" -> isLocked
    "BLIND" -> openingLevel > 0
    "THERMOSTAT" -> isHeatingOn
    "SMOKE_SENSOR" -> isSmokeDetected
    "WATER_LEAK_SENSOR" -> isLeakDetected
    "CONTACT_SENSOR" -> isContactOpen
    "TEMPERATURE_SENSOR" -> true
    else -> false
}

private val ACTIONABLE_TYPES = setOf(
    "LIGHT", "LOCK", "SWITCH", "SMART_TV", "BLIND", "THERMOSTAT"
)
