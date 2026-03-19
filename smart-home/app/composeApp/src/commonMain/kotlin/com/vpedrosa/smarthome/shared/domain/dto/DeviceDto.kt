package com.vpedrosa.smarthome.shared.domain.dto

import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.Color
import com.vpedrosa.smarthome.shared.domain.model.ContactSensor
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.shared.domain.model.Lock
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.shared.domain.model.SmokeSensor
import com.vpedrosa.smarthome.shared.domain.model.Switch
import com.vpedrosa.smarthome.shared.domain.model.TemperatureSensor
import com.vpedrosa.smarthome.shared.domain.model.Thermostat
import com.vpedrosa.smarthome.shared.domain.model.WaterLeakSensor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface DeviceDto {
    val id: String
    val name: String
    val roomId: String?

    fun toDomain(): Device
}

@Serializable
@SerialName("light")
data class LightDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val isOn: Boolean,
    val red: Int,
    val green: Int,
    val blue: Int,
    val brightness: Int,
) : DeviceDto {
    override fun toDomain(): Light = Light(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        isOn = isOn,
        color = Color(red, green, blue),
        brightness = brightness,
    )
}

@Serializable
@SerialName("lock")
data class LockDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val isLocked: Boolean,
) : DeviceDto {
    override fun toDomain(): Lock = Lock(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        isLocked = isLocked,
    )
}

@Serializable
@SerialName("blind")
data class BlindDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val openingLevel: Int,
) : DeviceDto {
    override fun toDomain(): Blind = Blind(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        openingLevel = openingLevel,
    )
}

@Serializable
@SerialName("switch")
data class SwitchDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val isOn: Boolean,
) : DeviceDto {
    override fun toDomain(): Switch = Switch(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        isOn = isOn,
    )
}

@Serializable
@SerialName("smoke_sensor")
data class SmokeSensorDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val isSmokeDetected: Boolean,
) : DeviceDto {
    override fun toDomain(): SmokeSensor = SmokeSensor(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        isSmokeDetected = isSmokeDetected,
    )
}

@Serializable
@SerialName("water_leak_sensor")
data class WaterLeakSensorDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val isLeakDetected: Boolean,
) : DeviceDto {
    override fun toDomain(): WaterLeakSensor = WaterLeakSensor(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        isLeakDetected = isLeakDetected,
    )
}

@Serializable
@SerialName("temperature_sensor")
data class TemperatureSensorDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val currentTemperature: Double,
) : DeviceDto {
    override fun toDomain(): TemperatureSensor = TemperatureSensor(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        currentTemperature = currentTemperature,
    )
}

@Serializable
@SerialName("contact_sensor")
data class ContactSensorDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val isOpen: Boolean,
) : DeviceDto {
    override fun toDomain(): ContactSensor = ContactSensor(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        isOpen = isOpen,
    )
}

@Serializable
@SerialName("thermostat")
data class ThermostatDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val currentTemperature: Double,
    val targetTemperature: Double,
    val isHeatingOn: Boolean,
) : DeviceDto {
    override fun toDomain(): Thermostat = Thermostat(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        currentTemperature = currentTemperature,
        targetTemperature = targetTemperature,
        isHeatingOn = isHeatingOn,
    )
}

@Serializable
@SerialName("smart_tv")
data class SmartTvDto(
    override val id: String,
    override val name: String,
    override val roomId: String?,
    val isOn: Boolean,
    val isCasting: Boolean,
) : DeviceDto {
    override fun toDomain(): SmartTv = SmartTv(
        id = DeviceId(id),
        name = name,
        roomId = roomId?.let { RoomId(it) },
        isOn = isOn,
        isCasting = isCasting,
    )
}

fun Device.toDto(): DeviceDto = when (this) {
    is Light -> LightDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        isOn = isOn,
        red = color.red,
        green = color.green,
        blue = color.blue,
        brightness = brightness,
    )
    is Lock -> LockDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        isLocked = isLocked,
    )
    is Blind -> BlindDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        openingLevel = openingLevel,
    )
    is Switch -> SwitchDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        isOn = isOn,
    )
    is SmokeSensor -> SmokeSensorDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        isSmokeDetected = isSmokeDetected,
    )
    is WaterLeakSensor -> WaterLeakSensorDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        isLeakDetected = isLeakDetected,
    )
    is TemperatureSensor -> TemperatureSensorDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        currentTemperature = currentTemperature,
    )
    is ContactSensor -> ContactSensorDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        isOpen = isOpen,
    )
    is Thermostat -> ThermostatDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        currentTemperature = currentTemperature,
        targetTemperature = targetTemperature,
        isHeatingOn = isHeatingOn,
    )
    is SmartTv -> SmartTvDto(
        id = id.value,
        name = name,
        roomId = roomId?.value,
        isOn = isOn,
        isCasting = isCasting,
    )
}
