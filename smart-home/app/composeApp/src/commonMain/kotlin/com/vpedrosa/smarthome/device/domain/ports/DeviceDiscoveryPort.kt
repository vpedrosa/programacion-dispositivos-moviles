package com.vpedrosa.smarthome.device.domain.ports

import com.vpedrosa.smarthome.device.domain.DiscoveredDevice
import kotlinx.coroutines.flow.Flow

interface DeviceDiscoveryPort {
    fun discoverDevices(): Flow<List<DiscoveredDevice>>
}
