package com.vpedrosa.smarthome.device.adapters.persistence

import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.DeviceType
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryDeviceRepository(
    initialDevices: List<Device> = DefaultDeviceData.devices,
) : DeviceRepository {

    private val store = MutableStateFlow(
        initialDevices.associateBy { it.id }
    )

    override fun observeAllDevices(): Flow<List<Device>> =
        store.map { it.values.toList() }

    override fun observeDevice(id: DeviceId): Flow<Device?> =
        store.map { it[id] }

    override fun observeDevicesByRoom(roomId: RoomId): Flow<List<Device>> =
        store.map { map ->
            map.values.filter { it.roomId == roomId }
        }

    override fun observeDevicesByType(type: DeviceType): Flow<List<Device>> =
        store.map { map ->
            map.values.filter { it.type == type }
        }

    override suspend fun save(device: Device) {
        store.update { it + (device.id to device) }
    }

    override suspend fun saveAll(devices: List<Device>) {
        store.update { current ->
            current + devices.associateBy { it.id }
        }
    }

    override suspend fun delete(id: DeviceId) {
        store.update { it - id }
    }
}
