package com.vpedrosa.smarthome.device.domain.dto

import com.vpedrosa.smarthome.device.domain.model.DeviceEvent
import com.vpedrosa.smarthome.device.domain.model.DeviceEventType
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import kotlin.time.Instant
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
