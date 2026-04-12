package com.vpedrosa.smarthome.device.domain

import com.vpedrosa.smarthome.device.domain.model.DeviceEvent
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import kotlinx.coroutines.flow.Flow

interface DeviceEventRepository {
    fun observeAllEvents(): Flow<List<DeviceEvent>>
    fun observeEventsByDevice(deviceId: DeviceId): Flow<List<DeviceEvent>>
    suspend fun add(event: DeviceEvent)
    suspend fun clear()
}
