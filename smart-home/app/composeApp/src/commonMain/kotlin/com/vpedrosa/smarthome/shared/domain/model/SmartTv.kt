package com.vpedrosa.smarthome.shared.domain.model

data class SmartTv(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isOn: Boolean,
    val contentUrl: String? = null,
) : Device {
    override val type: DeviceType get() = DeviceType.SMART_TV

    val isCasting: Boolean get() = contentUrl != null

    fun toggle(): SmartTv = copy(isOn = !isOn)

    fun launchContent(url: String): SmartTv = copy(contentUrl = url)

    fun stopCasting(): SmartTv = copy(contentUrl = null)
}
