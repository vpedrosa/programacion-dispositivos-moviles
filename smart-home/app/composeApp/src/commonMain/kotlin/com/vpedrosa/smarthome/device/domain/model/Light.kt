package com.vpedrosa.smarthome.device.domain.model

data class Light(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isOn: Boolean,
    val color: Color,
    val brightness: Int,
) : Device {
    override val type: DeviceType get() = DeviceType.LIGHT

    init {
        require(brightness in 0..100) { "Brightness must be in 0..100, was $brightness" }
    }

    fun toggle(): Light = copy(isOn = !isOn)

    fun changeBrightness(level: Int): Light = copy(brightness = level)

    fun changeColor(newColor: Color): Light = copy(color = newColor)
}
