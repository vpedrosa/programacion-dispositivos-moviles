package com.vpedrosa.smarthome.room.domain.model

import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.RoomId

data class Room(
    val id: RoomId,
    val name: String,
    val photoUri: String?,
    val deviceIds: List<DeviceId>,
) {
    fun addDevice(deviceId: DeviceId): Room =
        if (deviceId in deviceIds) this else copy(deviceIds = deviceIds + deviceId)

    fun removeDevice(deviceId: DeviceId): Room =
        copy(deviceIds = deviceIds - deviceId)
}
