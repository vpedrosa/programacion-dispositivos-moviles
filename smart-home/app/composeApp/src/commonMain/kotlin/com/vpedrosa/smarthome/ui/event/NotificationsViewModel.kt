package com.vpedrosa.smarthome.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.model.DeviceEvent
import com.vpedrosa.smarthome.device.domain.model.DeviceEventType
import com.vpedrosa.smarthome.device.domain.DeviceEventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class NotificationsUiState(
    val groupedEvents: List<EventGroup> = emptyList(),
)

data class EventGroup(
    val label: String,
    val events: List<EventItem>,
)

data class EventItem(
    val id: String,
    val message: String,
    val timeAgo: String,
    val subtitle: String,
    val type: DeviceEventType,
)

class NotificationsViewModel(
    deviceEventRepository: DeviceEventRepository,
) : ViewModel() {

    val uiState: StateFlow<NotificationsUiState> = deviceEventRepository.observeAllEvents()
        .map { events ->
            val now = Clock.System.now()
            val tz = TimeZone.currentSystemDefault()
            val todayDate = now.toLocalDateTime(tz).date

            val sorted = events.sortedByDescending { it.timestamp }

            val groups = sorted.groupBy { event ->
                val eventDate = event.timestamp.toLocalDateTime(tz).date
                val daysDiff = todayDate.toEpochDays() - eventDate.toEpochDays()
                when {
                    daysDiff == 0L -> "HOY"
                    daysDiff == 1L -> "AYER"
                    else -> eventDate.toString()
                }
            }.map { (label, eventsInGroup) ->
                EventGroup(
                    label = label,
                    events = eventsInGroup.map { it.toEventItem(now) },
                )
            }

            NotificationsUiState(groupedEvents = groups)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationsUiState(),
        )
}

private fun DeviceEvent.toEventItem(now: kotlin.time.Instant): EventItem {
    val diffMs = (now - timestamp).inWholeMilliseconds
    val diffMin = diffMs / 60_000
    val diffHours = diffMin / 60

    val timeAgo = when {
        diffMin < 1 -> "Ahora"
        diffMin < 60 -> "Hace $diffMin min"
        diffHours < 24 -> "Hace $diffHours h"
        else -> "Hace ${diffHours / 24} d"
    }

    val subtitle = when (type) {
        DeviceEventType.SMOKE_ALERT -> "Sensor de humo"
        DeviceEventType.WATER_LEAK_ALERT -> "Sensor de fugas"
        DeviceEventType.TEMPERATURE_READING -> "Sensor de temperatura"
        DeviceEventType.DOOR_OPENED, DeviceEventType.DOOR_CLOSED -> "Sensor de contacto"
        DeviceEventType.DOOR_OPEN_TOO_LONG -> "Sensor de contacto"
        DeviceEventType.THERMOSTAT_ADJUSTED -> "Termostato"
        DeviceEventType.DEVICE_TURNED_ON, DeviceEventType.DEVICE_TURNED_OFF -> "Dispositivo"
    }

    return EventItem(
        id = id,
        message = message,
        timeAgo = "$timeAgo \u00B7 $subtitle",
        subtitle = subtitle,
        type = type,
    )
}
