package com.vpedrosa.smarthome.shared.domain.model

sealed interface Device {
    val id: DeviceId
    val name: String
    val roomId: RoomId?
    val type: DeviceType
}
