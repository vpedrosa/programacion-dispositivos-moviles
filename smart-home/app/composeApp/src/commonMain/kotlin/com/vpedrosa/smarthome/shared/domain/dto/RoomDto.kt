package com.vpedrosa.smarthome.shared.domain.dto

import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.Room
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import kotlinx.serialization.Serializable

@Serializable
data class RoomDto(
    val id: String,
    val name: String,
    val photoUri: String?,
    val deviceIds: List<String>,
) {
    fun toDomain(): Room = Room(
        id = RoomId(id),
        name = name,
        photoUri = photoUri,
        deviceIds = deviceIds.map { DeviceId(it) },
    )
}

fun Room.toDto(): RoomDto = RoomDto(
    id = id.value,
    name = name,
    photoUri = photoUri,
    deviceIds = deviceIds.map { it.value },
)
