package com.vpedrosa.smarthome.device.domain.model

/**
 * Whether this device type supports toggling (on/off or lock/unlock).
 * Delegates to [DeviceTypeRegistry].
 */
fun DeviceType.isToggleable(): Boolean = DeviceTypeRegistry.isToggleable(this)

/**
 * Whether this device type supports bulk toggle actions.
 * Delegates to [DeviceTypeRegistry].
 */
fun DeviceType.supportsBulkToggle(): Boolean = DeviceTypeRegistry.supportsBulkToggle(this)

/**
 * Whether this individual device is currently in its "on" / "active" state.
 */
fun Device.isActive(): Boolean = when (this) {
    is Light -> isOn
    is Lock -> isLocked
    is Switch -> isOn
    is SmartTv -> isOn
    is Thermostat -> isHeatingOn
    is ContactSensor -> isOpen
    is Blind -> openingLevel > 0
    is SmokeSensor -> isSmokeDetected
    is WaterLeakSensor -> isLeakDetected
    is TemperatureSensor -> true
}

/**
 * Returns a human-readable subtitle for the device state.
 */
fun Device.stateLabel(): String = when (this) {
    is Light -> if (isOn) "ON - ${brightness}%" else "OFF"
    is Lock -> if (isLocked) "Locked" else "Unlocked"
    is Switch -> if (isOn) "ON" else "OFF"
    is SmartTv -> if (isOn) "ON" else "OFF"
    is Thermostat -> if (isHeatingOn) "${currentTemperature}C" else "OFF"
    is ContactSensor -> if (isOpen) "Open" else "Closed"
    is Blind -> "${openingLevel}%"
    is SmokeSensor -> if (isSmokeDetected) "ALERT" else "OK"
    is WaterLeakSensor -> if (isLeakDetected) "ALERT" else "OK"
    is TemperatureSensor -> "${currentTemperature}C"
}

/**
 * Whether all devices in a list are in their active state.
 */
fun List<Device>.allActive(): Boolean = all { it.isActive() }
