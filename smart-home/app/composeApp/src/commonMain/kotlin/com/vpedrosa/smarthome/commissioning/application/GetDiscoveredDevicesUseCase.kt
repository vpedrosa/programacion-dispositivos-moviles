package com.vpedrosa.smarthome.commissioning.application

import com.vpedrosa.smarthome.commissioning.domain.DeviceDiscoveryPort
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import kotlinx.coroutines.flow.Flow

class GetDiscoveredDevicesUseCase(
    private val discoveryPort: DeviceDiscoveryPort,
) {
    operator fun invoke(): Flow<List<DiscoveredDevice>> =
        discoveryPort.discoverDevices()
}
