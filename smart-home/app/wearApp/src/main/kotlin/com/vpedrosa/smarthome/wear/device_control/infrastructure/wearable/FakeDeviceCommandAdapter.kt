package com.vpedrosa.smarthome.wear.device_control.infrastructure.wearable

import com.vpedrosa.smarthome.wear.device_control.domain.ActionResult
import com.vpedrosa.smarthome.wear.device_control.domain.DeviceCommandPort
import com.vpedrosa.smarthome.wear.device_control.domain.DeviceListResult
import com.vpedrosa.smarthome.wear.device_control.domain.model.WearDevice

/**
 * Fake adapter for testing and emulators without Play Services.
 */
class FakeDeviceCommandAdapter : DeviceCommandPort {

    private val devices = mutableListOf(
        WearDevice(id = "1", name = "Luz principal", type = "LIGHT", roomName = "Salón", isOn = true),
        WearDevice(id = "2", name = "Smart TV", type = "SMART_TV", roomName = "Salón", isOn = false),
        WearDevice(id = "3", name = "Luz cocina", type = "LIGHT", roomName = "Cocina", isOn = false),
        WearDevice(id = "4", name = "Puerta principal", type = "LOCK", roomName = "Entrada", isLocked = true),
        WearDevice(id = "5", name = "Termostato", type = "THERMOSTAT", roomName = "Dormitorio",
            currentTemperature = 22.0, targetTemperature = 24.0, isHeatingOn = true),
        WearDevice(id = "6", name = "Persiana", type = "BLIND", roomName = "Dormitorio", openingLevel = 70),
        WearDevice(id = "7", name = "Sensor humo", type = "SMOKE_SENSOR", roomName = "Cocina"),
        WearDevice(id = "8", name = "Luz pasillo", type = "LIGHT", roomName = null, isOn = true),
    )

    override suspend fun requestDeviceList(): DeviceListResult {
        return DeviceListResult.Success(devices.toList())
    }

    override suspend fun sendToggleAction(deviceId: String): ActionResult {
        val index = devices.indexOfFirst { it.id == deviceId }
        if (index == -1) return ActionResult.Error("Device not found")

        val device = devices[index]
        val toggled = when (device.type) {
            "LIGHT", "SWITCH", "SMART_TV" -> device.copy(isOn = !device.isOn)
            "LOCK" -> device.copy(isLocked = !device.isLocked)
            "BLIND" -> device.copy(openingLevel = if (device.openingLevel > 0) 0 else 100)
            "THERMOSTAT" -> device.copy(isHeatingOn = !device.isHeatingOn)
            else -> return ActionResult.Error("Device is read-only")
        }
        devices[index] = toggled
        return ActionResult.Success(toggled)
    }
}
