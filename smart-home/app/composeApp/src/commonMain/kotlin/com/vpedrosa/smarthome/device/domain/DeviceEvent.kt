package com.vpedrosa.smarthome.device.domain

import kotlinx.datetime.Instant

data class DeviceEvent(
    val id: String,
    val deviceId: DeviceId,
    val type: DeviceEventType,
    val message: String,
    val timestamp: Instant,
)

enum class DeviceEventType {
    SMOKE_ALERT,
    WATER_LEAK_ALERT,
    TEMPERATURE_READING,
    DOOR_OPENED,
    DOOR_CLOSED,
    DOOR_OPEN_TOO_LONG,
    THERMOSTAT_ADJUSTED,
    DEVICE_TURNED_ON,
    DEVICE_TURNED_OFF,
}
