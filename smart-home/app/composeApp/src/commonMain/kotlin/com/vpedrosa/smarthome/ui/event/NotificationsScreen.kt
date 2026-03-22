package com.vpedrosa.smarthome.ui.event

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SensorDoor
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.StringResource
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.notifications_title
import smarthome.composeapp.generated.resources.notifications_empty
import smarthome.composeapp.generated.resources.a11y_alert_smoke
import smarthome.composeapp.generated.resources.a11y_alert_water
import smarthome.composeapp.generated.resources.a11y_alert_temperature
import smarthome.composeapp.generated.resources.a11y_alert_door
import smarthome.composeapp.generated.resources.a11y_alert_door_open_too_long
import smarthome.composeapp.generated.resources.a11y_alert_thermostat
import com.vpedrosa.smarthome.ui.theme.AlertRed
import com.vpedrosa.smarthome.ui.theme.AlertOrange
import com.vpedrosa.smarthome.ui.theme.AlertYellow
import com.vpedrosa.smarthome.ui.theme.AlertBlue
import com.vpedrosa.smarthome.ui.theme.AlertGreen
import com.vpedrosa.smarthome.ui.theme.CardBackground

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(Res.string.notifications_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.groupedEvents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.notifications_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.groupedEvents.forEach { group ->
                    item(key = "header-${group.label}") {
                        Text(
                            text = group.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                    items(group.events, key = { it.id }) { event ->
                        EventCard(event)
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: EventItem) {
    val (iconColor, icon) = eventIconData(event.type)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = event.type.a11yRes()?.let { stringResource(it) },
                    tint = iconColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = event.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        }
    }
}

private fun eventIconData(type: DeviceEventType): Pair<Color, ImageVector> = when (type) {
    DeviceEventType.SMOKE_ALERT -> AlertRed to Icons.Default.LocalFireDepartment
    DeviceEventType.WATER_LEAK_ALERT -> AlertBlue to Icons.Default.WaterDrop
    DeviceEventType.TEMPERATURE_READING -> AlertYellow to Icons.Default.Thermostat
    DeviceEventType.DOOR_OPENED -> AlertOrange to Icons.Default.SensorDoor
    DeviceEventType.DOOR_CLOSED -> AlertGreen to Icons.Default.SensorDoor
    DeviceEventType.DOOR_OPEN_TOO_LONG -> AlertRed to Icons.Default.Warning
    DeviceEventType.THERMOSTAT_ADJUSTED -> AlertBlue to Icons.Default.Thermostat
    DeviceEventType.DEVICE_TURNED_ON -> AlertGreen to Icons.Default.PowerSettingsNew
    DeviceEventType.DEVICE_TURNED_OFF -> AlertOrange to Icons.Default.PowerSettingsNew
}

private fun DeviceEventType.a11yRes(): StringResource? = when (this) {
    DeviceEventType.SMOKE_ALERT -> Res.string.a11y_alert_smoke
    DeviceEventType.WATER_LEAK_ALERT -> Res.string.a11y_alert_water
    DeviceEventType.TEMPERATURE_READING -> Res.string.a11y_alert_temperature
    DeviceEventType.DOOR_OPENED, DeviceEventType.DOOR_CLOSED -> Res.string.a11y_alert_door
    DeviceEventType.DOOR_OPEN_TOO_LONG -> Res.string.a11y_alert_door_open_too_long
    DeviceEventType.THERMOSTAT_ADJUSTED -> Res.string.a11y_alert_thermostat
    DeviceEventType.DEVICE_TURNED_ON, DeviceEventType.DEVICE_TURNED_OFF -> null
}
