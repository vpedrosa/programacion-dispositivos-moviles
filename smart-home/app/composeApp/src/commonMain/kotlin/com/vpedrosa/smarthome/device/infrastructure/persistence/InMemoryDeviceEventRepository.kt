package com.vpedrosa.smarthome.device.infrastructure.persistence

import com.vpedrosa.smarthome.device.domain.model.DeviceEvent
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.DeviceEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryDeviceEventRepository : DeviceEventRepository {

    private val store = MutableStateFlow<List<DeviceEvent>>(emptyList())

    override fun observeAllEvents(): Flow<List<DeviceEvent>> = store

    override fun observeEventsByDevice(deviceId: DeviceId): Flow<List<DeviceEvent>> =
        store.map { events ->
            events.filter { it.deviceId == deviceId }
        }

    override suspend fun add(event: DeviceEvent) {
        store.update { it + event }
    }

    override suspend fun clear() {
        store.update { emptyList() }
    }
}
