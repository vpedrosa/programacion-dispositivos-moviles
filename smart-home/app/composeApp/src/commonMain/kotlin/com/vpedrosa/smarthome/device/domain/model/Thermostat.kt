package com.vpedrosa.smarthome.device.domain.model

data class Thermostat(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val currentTemperature: Double,
    val targetTemperature: Double,
    val isHeatingOn: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.THERMOSTAT

    fun adjustTarget(temperature: Double): Thermostat = copy(targetTemperature = temperature)

    fun toggleHeating(): Thermostat = copy(isHeatingOn = !isHeatingOn)
}
