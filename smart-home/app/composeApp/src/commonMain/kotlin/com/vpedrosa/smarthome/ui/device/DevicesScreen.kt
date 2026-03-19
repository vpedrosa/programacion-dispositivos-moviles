package com.vpedrosa.smarthome.ui.device

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Blinds
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.allActive
import com.vpedrosa.smarthome.shared.domain.model.isActive
import com.vpedrosa.smarthome.shared.domain.model.isToggleable
import com.vpedrosa.smarthome.shared.domain.model.stateLabel
import com.vpedrosa.smarthome.shared.domain.model.supportsBulkToggle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.action_close_all
import smarthome.composeapp.generated.resources.action_open_all
import smarthome.composeapp.generated.resources.action_turn_off_all
import smarthome.composeapp.generated.resources.action_turn_on_all
import smarthome.composeapp.generated.resources.device_type_blinds
import smarthome.composeapp.generated.resources.device_type_contact_sensors
import smarthome.composeapp.generated.resources.device_type_lights
import smarthome.composeapp.generated.resources.device_type_locks
import smarthome.composeapp.generated.resources.device_type_smart_tvs
import smarthome.composeapp.generated.resources.device_type_smoke_sensors
import smarthome.composeapp.generated.resources.device_type_switches
import smarthome.composeapp.generated.resources.device_type_temperature_sensors
import smarthome.composeapp.generated.resources.device_type_thermostats
import smarthome.composeapp.generated.resources.device_type_water_leak_sensors
import smarthome.composeapp.generated.resources.a11y_navigate_back
import smarthome.composeapp.generated.resources.title_devices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    onNavigateToDeviceDetail: (String) -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    viewModel: DevicesViewModel = koinViewModel(),
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
                    text = stringResource(Res.string.title_devices),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                if (onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.a11y_navigate_back),
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.devicesByType.forEach { (type, devices) ->
                item(key = "header_$type") {
                    DeviceCategorySectionHeader(
                        type = type,
                        count = devices.size,
                    )
                }

                items(
                    items = devices,
                    key = { it.id.value },
                ) { device ->
                    DeviceRow(
                        device = device,
                        roomName = device.roomId?.let { state.roomNames[it] },
                        isToggleable = type.isToggleable(),
                        onToggle = { viewModel.onToggleDevice(device.id) },
                        onClick = { onNavigateToDeviceDetail(device.id.value) },
                    )
                }

                if (type.supportsBulkToggle()) {
                    item(key = "bulk_$type") {
                        if (type == DeviceType.BLIND) {
                            BulkToggleButton(
                                allActive = devices.allActive(),
                                onBulkToggle = { turnOn -> viewModel.onBulkToggle(type, turnOn) },
                                activeLabel = stringResource(Res.string.action_close_all),
                                inactiveLabel = stringResource(Res.string.action_open_all),
                            )
                        } else {
                            BulkToggleButton(
                                allActive = devices.allActive(),
                                onBulkToggle = { turnOn -> viewModel.onBulkToggle(type, turnOn) },
                            )
                        }
                    }
                }

                item(key = "spacer_$type") {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
internal fun DeviceCategorySectionHeader(
    type: DeviceType,
    count: Int,
) {
    val label = stringResource(type.pluralLabelRes())

    Text(
        text = "$label ($count)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
internal fun DeviceRow(
    device: Device,
    roomName: String?,
    isToggleable: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = device.type.icon(),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = buildSubtitle(roomName, device.stateLabel()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }

            if (isToggleable) {
                Switch(
                    checked = device.isActive(),
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                )
            }
        }
    }
}

@Composable
internal fun BulkToggleButton(
    allActive: Boolean,
    onBulkToggle: (turnOn: Boolean) -> Unit,
    activeLabel: String = stringResource(Res.string.action_turn_off_all),
    inactiveLabel: String = stringResource(Res.string.action_turn_on_all),
) {
    val label = if (allActive) activeLabel else inactiveLabel

    OutlinedButton(
        onClick = { onBulkToggle(!allActive) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(text = label)
    }
}

internal fun buildSubtitle(roomName: String?, stateLabel: String): String =
    if (roomName != null) "$roomName - $stateLabel" else stateLabel

internal fun DeviceType.icon(): ImageVector = when (this) {
    DeviceType.LIGHT -> Icons.Default.Lightbulb
    DeviceType.LOCK -> Icons.Default.Lock
    DeviceType.BLIND -> Icons.Default.Blinds
    DeviceType.SWITCH -> Icons.Default.ToggleOn
    DeviceType.SMOKE_SENSOR -> Icons.Default.Sensors
    DeviceType.WATER_LEAK_SENSOR -> Icons.Default.Water
    DeviceType.TEMPERATURE_SENSOR -> Icons.Default.DeviceThermostat
    DeviceType.CONTACT_SENSOR -> Icons.Default.Sensors
    DeviceType.THERMOSTAT -> Icons.Default.Thermostat
    DeviceType.SMART_TV -> Icons.Default.Tv
}

internal fun DeviceType.pluralLabelRes(): StringResource = when (this) {
    DeviceType.LIGHT -> Res.string.device_type_lights
    DeviceType.LOCK -> Res.string.device_type_locks
    DeviceType.BLIND -> Res.string.device_type_blinds
    DeviceType.SWITCH -> Res.string.device_type_switches
    DeviceType.SMOKE_SENSOR -> Res.string.device_type_smoke_sensors
    DeviceType.WATER_LEAK_SENSOR -> Res.string.device_type_water_leak_sensors
    DeviceType.TEMPERATURE_SENSOR -> Res.string.device_type_temperature_sensors
    DeviceType.CONTACT_SENSOR -> Res.string.device_type_contact_sensors
    DeviceType.THERMOSTAT -> Res.string.device_type_thermostats
    DeviceType.SMART_TV -> Res.string.device_type_smart_tvs
}
