package com.vpedrosa.smarthome.device.domain.model

data class Blind(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val openingLevel: Int,
) : Device {
    override val type: DeviceType get() = DeviceType.BLIND

    init {
        require(openingLevel in 0..100) { "Opening level must be in 0..100, was $openingLevel" }
    }

    fun changeOpeningLevel(level: Int): Blind = copy(openingLevel = level)
}
