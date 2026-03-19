package com.vpedrosa.smarthome.commissioning.domain

import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import kotlinx.coroutines.flow.Flow

interface DeviceDiscoveryPort {
    fun discoverDevices(): Flow<List<DiscoveredDevice>>
}
