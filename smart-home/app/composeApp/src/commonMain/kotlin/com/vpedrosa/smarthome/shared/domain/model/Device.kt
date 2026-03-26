package com.vpedrosa.smarthome.shared.domain.model

sealed interface Device {
    val id: DeviceId
    val name: String
    val roomId: RoomId?
    val type: DeviceType
}

fun Device.withName(newName: String): Device = when (this) {
    is Light -> copy(name = newName)
    is Lock -> copy(name = newName)
    is Blind -> copy(name = newName)
    is Switch -> copy(name = newName)
    is SmokeSensor -> copy(name = newName)
    is WaterLeakSensor -> copy(name = newName)
    is TemperatureSensor -> copy(name = newName)
    is ContactSensor -> copy(name = newName)
    is Thermostat -> copy(name = newName)
    is SmartTv -> copy(name = newName)
}
