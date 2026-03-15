package com.vpedrosa.smarthome.device.domain

data class ContactSensor(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isOpen: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.CONTACT_SENSOR

    fun toggle(): ContactSensor = copy(isOpen = !isOpen)
}
