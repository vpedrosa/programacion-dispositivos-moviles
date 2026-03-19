package com.vpedrosa.smarthome.shared.domain.model

data class SmartTv(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isOn: Boolean,
    val isCasting: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.SMART_TV

    fun toggle(): SmartTv = copy(isOn = !isOn)

    fun toggleCasting(): SmartTv = copy(isCasting = !isCasting)
}
