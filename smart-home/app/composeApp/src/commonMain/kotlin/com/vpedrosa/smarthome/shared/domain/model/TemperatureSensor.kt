package com.vpedrosa.smarthome.shared.domain.model

data class TemperatureSensor(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val currentTemperature: Double,
) : Device {
    override val type: DeviceType get() = DeviceType.TEMPERATURE_SENSOR
}
