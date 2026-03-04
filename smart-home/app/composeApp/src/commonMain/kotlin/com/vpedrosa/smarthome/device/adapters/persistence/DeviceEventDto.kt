package com.vpedrosa.smarthome.device.adapters.persistence

import com.vpedrosa.smarthome.device.domain.DeviceEvent
import com.vpedrosa.smarthome.device.domain.DeviceEventType
import com.vpedrosa.smarthome.device.domain.DeviceId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class DeviceEventDto(
    val id: String,
    val deviceId: String,
    val type: String,
    val message: String,
    val timestamp: String,
) {
    fun toDomain(): DeviceEvent = DeviceEvent(
        id = id,
        deviceId = DeviceId(deviceId),
        type = DeviceEventType.valueOf(type),
        message = message,
        timestamp = Instant.parse(timestamp),
    )
}

fun DeviceEvent.toDto(): DeviceEventDto = DeviceEventDto(
    id = id,
    deviceId = deviceId.value,
    type = type.name,
    message = message,
    timestamp = timestamp.toString(),
)
