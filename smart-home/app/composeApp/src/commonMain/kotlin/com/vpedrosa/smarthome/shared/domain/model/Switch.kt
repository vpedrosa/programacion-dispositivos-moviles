package com.vpedrosa.smarthome.shared.domain.model

data class Switch(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isOn: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.SWITCH

    fun toggle(): Switch = copy(isOn = !isOn)
}
