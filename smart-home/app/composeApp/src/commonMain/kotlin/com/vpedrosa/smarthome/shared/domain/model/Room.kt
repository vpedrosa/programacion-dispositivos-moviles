package com.vpedrosa.smarthome.shared.domain.model

@JvmInline
value class RoomId(val value: String)

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
