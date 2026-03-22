package com.vpedrosa.smarthome.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.LightbulbCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import com.vpedrosa.smarthome.ui.components.UriImage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import smarthome.composeapp.generated.resources.Res
import smarthome.composeapp.generated.resources.dashboard_greeting
import smarthome.composeapp.generated.resources.dashboard_home_name
import smarthome.composeapp.generated.resources.dashboard_lights_on
import smarthome.composeapp.generated.resources.dashboard_locks_active
import smarthome.composeapp.generated.resources.dashboard_recent_alerts
import smarthome.composeapp.generated.resources.dashboard_rooms
import smarthome.composeapp.generated.resources.dashboard_smart_tv
import smarthome.composeapp.generated.resources.dashboard_temperature
import smarthome.composeapp.generated.resources.dashboard_no_alerts
import smarthome.composeapp.generated.resources.dashboard_active_devices
import smarthome.composeapp.generated.resources.action_on
import smarthome.composeapp.generated.resources.action_off
import smarthome.composeapp.generated.resources.a11y_icon_lights
import smarthome.composeapp.generated.resources.a11y_icon_locks
import smarthome.composeapp.generated.resources.a11y_icon_temperature
import smarthome.composeapp.generated.resources.a11y_icon_smart_tv
import smarthome.composeapp.generated.resources.a11y_icon_home
import smarthome.composeapp.generated.resources.a11y_room_placeholder
import smarthome.composeapp.generated.resources.commissioning_devices_button
import smarthome.composeapp.generated.resources.dashboard_voice_control
import com.vpedrosa.smarthome.ui.theme.Navy
import com.vpedrosa.smarthome.ui.theme.CardBackground
import com.vpedrosa.smarthome.ui.theme.AlertGreen
import com.vpedrosa.smarthome.ui.theme.AlertOrange
import com.vpedrosa.smarthome.ui.theme.AlertRed
import com.vpedrosa.smarthome.ui.theme.AlertBlue

@Composable
fun DashboardScreen(
    onNavigateToCommissioning: () -> Unit = {},
    onNavigateToVoiceControl: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // -- Greeting header --
        GreetingHeader()

        Spacer(modifier = Modifier.height(12.dp))

        // -- Quick commissioning button --
        Button(
            onClick = onNavigateToCommissioning,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Navy,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.commissioning_devices_button))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // -- Voice control button --
        Button(
            onClick = onNavigateToVoiceControl,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Navy,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.dashboard_voice_control))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -- Summary cards 2x2 grid --
        SummaryGrid(state)

        Spacer(modifier = Modifier.height(24.dp))

        // -- Recent alerts --
        SectionTitle(stringResource(Res.string.dashboard_recent_alerts))
        Spacer(modifier = Modifier.height(8.dp))
        RecentAlerts(state.recentAlerts)

        Spacer(modifier = Modifier.height(24.dp))

        // -- Rooms --
        SectionTitle(stringResource(Res.string.dashboard_rooms))
        Spacer(modifier = Modifier.height(8.dp))
        RoomsRow(state.rooms)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GreetingHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.dashboard_greeting),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(Res.string.a11y_icon_home),
                    tint = Navy,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.dashboard_home_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy,
                )
            }
        }
    }
}

@Composable
private fun SummaryGrid(state: DashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LightbulbCircle,
                iconDescription = stringResource(Res.string.a11y_icon_lights),
                value = "${state.lightsOnCount}",
                label = stringResource(Res.string.dashboard_lights_on),
                iconTint = Color(0xFFFFC107),
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Lock,
                iconDescription = stringResource(Res.string.a11y_icon_locks),
                value = "${state.locksCount}",
                label = stringResource(Res.string.dashboard_locks_active),
                iconTint = Navy,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Thermostat,
                iconDescription = stringResource(Res.string.a11y_icon_temperature),
                value = state.temperature,
                label = stringResource(Res.string.dashboard_temperature),
                iconTint = AlertOrange,
            )
            if (state.hasSmartTv) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Tv,
                    iconDescription = stringResource(Res.string.a11y_icon_smart_tv),
                    value = if (state.isSmartTvOn) stringResource(Res.string.action_on) else stringResource(Res.string.action_off),
                    label = stringResource(Res.string.dashboard_smart_tv),
                    iconTint = AlertBlue,
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconDescription: String,
    value: String,
    label: String,
    iconTint: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = iconTint,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        letterSpacing = MaterialTheme.typography.titleSmall.letterSpacing,
    )
}

@Composable
private fun RecentAlerts(alerts: List<AlertItem>) {
    if (alerts.isEmpty()) {
        Text(
            text = stringResource(Res.string.dashboard_no_alerts),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            alerts.forEach { alert ->
                val chipColor = alertColor(alert.type)
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = chipColor.copy(alpha = 0.15f),
                        labelColor = chipColor,
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = chipColor.copy(alpha = 0.3f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun RoomsRow(rooms: List<RoomSummary>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 8.dp),
    ) {
        items(rooms, key = { it.id }) { room ->
            RoomCard(room)
        }
    }
}

@Composable
private fun RoomCard(room: RoomSummary) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(140.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                if (room.photoUri != null) {
                    UriImage(
                        uri = room.photoUri,
                        contentDescription = room.name,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(Res.string.a11y_room_placeholder),
                        tint = Navy.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(Res.string.dashboard_active_devices, room.activeDeviceCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        }
    }
}

private fun alertColor(type: DeviceEventType): Color = when (type) {
    DeviceEventType.SMOKE_ALERT -> AlertRed
    DeviceEventType.WATER_LEAK_ALERT -> AlertBlue
    DeviceEventType.DOOR_OPENED -> AlertOrange
    DeviceEventType.DOOR_CLOSED -> AlertGreen
    DeviceEventType.DOOR_OPEN_TOO_LONG -> AlertRed
    DeviceEventType.TEMPERATURE_READING -> Color(0xFFFFC107)
    DeviceEventType.THERMOSTAT_ADJUSTED -> AlertBlue
    DeviceEventType.DEVICE_TURNED_ON -> AlertGreen
    DeviceEventType.DEVICE_TURNED_OFF -> AlertOrange
}
