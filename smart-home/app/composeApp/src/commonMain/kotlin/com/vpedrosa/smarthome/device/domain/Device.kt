package com.vpedrosa.smarthome.device.domain

sealed interface Device {
    val id: DeviceId
    val name: String
    val roomId: RoomId?
    val type: DeviceType
}

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

data class Lock(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isLocked: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.LOCK

    fun toggle(): Lock = copy(isLocked = !isLocked)
}

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

data class Switch(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isOn: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.SWITCH

    fun toggle(): Switch = copy(isOn = !isOn)
}

data class SmokeSensor(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isSmokeDetected: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.SMOKE_SENSOR
}

data class WaterLeakSensor(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isLeakDetected: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.WATER_LEAK_SENSOR
}

data class TemperatureSensor(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val currentTemperature: Double,
) : Device {
    override val type: DeviceType get() = DeviceType.TEMPERATURE_SENSOR
}

data class ContactSensor(
    override val id: DeviceId,
    override val name: String,
    override val roomId: RoomId?,
    val isOpen: Boolean,
) : Device {
    override val type: DeviceType get() = DeviceType.CONTACT_SENSOR

    fun toggle(): ContactSensor = copy(isOpen = !isOpen)
}

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
