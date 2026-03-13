package com.vpedrosa.smarthome.device.domain.ports

import com.vpedrosa.smarthome.device.domain.DeviceId

interface DeviceControlPort {
    suspend fun toggleOnOff(deviceId: DeviceId, on: Boolean)
    suspend fun setLevel(deviceId: DeviceId, level: Int)
    suspend fun lockDoor(deviceId: DeviceId, lock: Boolean)
    suspend fun setThermostatSetpoint(deviceId: DeviceId, temperatureCelsius: Double)
}
