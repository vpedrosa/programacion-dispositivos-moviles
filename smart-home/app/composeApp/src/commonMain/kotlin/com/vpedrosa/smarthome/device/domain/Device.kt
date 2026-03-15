package com.vpedrosa.smarthome.device.domain

sealed interface Device {
    val id: DeviceId
    val name: String
    val roomId: RoomId?
    val type: DeviceType
}
