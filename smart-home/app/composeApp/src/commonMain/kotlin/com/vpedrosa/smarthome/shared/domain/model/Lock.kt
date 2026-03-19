package com.vpedrosa.smarthome.shared.domain.model

data class Lock(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isLocked: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.LOCK

    fun toggle(): Lock = copy(isLocked = !isLocked)
}
