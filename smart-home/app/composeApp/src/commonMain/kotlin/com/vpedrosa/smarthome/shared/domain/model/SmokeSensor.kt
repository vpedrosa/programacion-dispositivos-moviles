package com.vpedrosa.smarthome.shared.domain.model

data class SmokeSensor(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isSmokeDetected: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.SMOKE_SENSOR
}
