package com.vpedrosa.smarthome.shared.domain

import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.RoomId
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
