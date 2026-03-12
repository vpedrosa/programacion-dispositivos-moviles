package com.vpedrosa.smarthome.device.domain.ports

import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DiscoveredDevice

interface CommissioningPort {
    suspend fun commission(device: DiscoveredDevice): Result<Device>
}
