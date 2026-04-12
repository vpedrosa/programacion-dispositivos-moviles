package com.vpedrosa.smarthome.commissioning.domain

import com.vpedrosa.smarthome.device.domain.model.Device
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice

interface CommissioningPort {
    suspend fun commission(device: DiscoveredDevice): Result<Device>
}
