package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.DiscoveredDevice
import com.vpedrosa.smarthome.device.domain.ports.DeviceDiscoveryPort
import kotlinx.coroutines.flow.Flow

class ObserveDiscoveredDevicesUseCase(
    private val discoveryPort: DeviceDiscoveryPort,
) {
    operator fun invoke(): Flow<List<DiscoveredDevice>> = discoveryPort.discoverDevices()
}
