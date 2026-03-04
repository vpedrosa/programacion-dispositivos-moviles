package com.vpedrosa.smarthome.device.domain.ports

import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.DeviceType
import com.vpedrosa.smarthome.device.domain.RoomId
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeAllDevices(): Flow<List<Device>>
    fun observeDevice(id: DeviceId): Flow<Device?>
    fun observeDevicesByRoom(roomId: RoomId): Flow<List<Device>>
    fun observeDevicesByType(type: DeviceType): Flow<List<Device>>
    suspend fun save(device: Device)
    suspend fun saveAll(devices: List<Device>)
    suspend fun delete(id: DeviceId)
}
