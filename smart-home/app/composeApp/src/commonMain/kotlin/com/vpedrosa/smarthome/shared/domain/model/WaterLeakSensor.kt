package com.vpedrosa.smarthome.shared.domain.model

data class WaterLeakSensor(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isLeakDetected: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.WATER_LEAK_SENSOR
}
