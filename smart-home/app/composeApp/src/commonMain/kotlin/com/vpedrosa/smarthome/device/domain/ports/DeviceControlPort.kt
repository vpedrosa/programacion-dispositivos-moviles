package com.vpedrosa.smarthome.device.domain.ports

import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.DiscoveredDevice

interface DeviceControlPort {
    fun registerDevice(deviceId: DeviceId, discoveredDevice: DiscoveredDevice)
    suspend fun toggleOnOff(deviceId: DeviceId, on: Boolean)
    suspend fun setLevel(deviceId: DeviceId, level: Int)
    suspend fun lockDoor(deviceId: DeviceId, lock: Boolean)
    suspend fun setThermostatSetpoint(deviceId: DeviceId, temperatureCelsius: Double)
    suspend fun setThermostatMode(deviceId: DeviceId, heating: Boolean)
    suspend fun setWindowCoveringPosition(deviceId: DeviceId, openPercent: Int)
}
