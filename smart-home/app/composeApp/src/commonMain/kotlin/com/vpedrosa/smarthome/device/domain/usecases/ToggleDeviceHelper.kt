package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Blind
import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.Lock
import com.vpedrosa.smarthome.device.domain.SmartTv
import com.vpedrosa.smarthome.device.domain.Switch
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.ports.DeviceControlPort

/**
 * Toggles a single device to the desired [turnOn] state via [controlPort],
 * returning the updated [Device] copy, or `null` if it was already in that state.
 */
internal suspend fun toggleDevice(
    device: Device,
    turnOn: Boolean,
    controlPort: DeviceControlPort,
): Device? = when (device) {
    is Light -> if (device.isOn != turnOn) {
        controlPort.toggleOnOff(device.id, turnOn)
        device.copy(isOn = turnOn)
    } else null

    is Switch -> if (device.isOn != turnOn) {
        controlPort.toggleOnOff(device.id, turnOn)
        device.copy(isOn = turnOn)
    } else null

    is SmartTv -> if (device.isOn != turnOn) {
        controlPort.toggleOnOff(device.id, turnOn)
        device.copy(isOn = turnOn)
    } else null

    is Lock -> if (device.isLocked != turnOn) {
        controlPort.lockDoor(device.id, turnOn)
        device.copy(isLocked = turnOn)
    } else null

    is Thermostat -> if (device.isHeatingOn != turnOn) {
        controlPort.setThermostatMode(device.id, turnOn)
        device.copy(isHeatingOn = turnOn)
    } else null

    is Blind -> {
        val targetLevel = if (turnOn) 100 else 0
        if (device.openingLevel != targetLevel) {
            controlPort.setWindowCoveringPosition(device.id, targetLevel)
            device.copy(openingLevel = targetLevel)
        } else null
    }

    else -> null
}
