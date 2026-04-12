package com.vpedrosa.smarthome.device.domain

import com.vpedrosa.smarthome.device.domain.model.Color
import com.vpedrosa.smarthome.device.domain.model.DeviceConnectionInfo
import com.vpedrosa.smarthome.device.domain.model.DeviceId

interface DeviceControlPort {
    fun registerDevice(deviceId: DeviceId, connectionInfo: DeviceConnectionInfo)
    fun deregisterDevice(deviceId: DeviceId)
    suspend fun toggleOnOff(deviceId: DeviceId, on: Boolean)
    suspend fun setLevel(deviceId: DeviceId, level: Int)
    suspend fun setColor(deviceId: DeviceId, color: Color)
    suspend fun lockDoor(deviceId: DeviceId, lock: Boolean)
    suspend fun setThermostatSetpoint(deviceId: DeviceId, temperatureCelsius: Double)
    suspend fun setThermostatMode(deviceId: DeviceId, heating: Boolean)
    suspend fun setWindowCoveringPosition(deviceId: DeviceId, openPercent: Int)
    suspend fun launchContent(deviceId: DeviceId, url: String)
}
