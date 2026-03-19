package com.vpedrosa.smarthome.commissioning.domain.model

import com.vpedrosa.smarthome.shared.domain.model.DeviceType

data class DiscoveredDevice(
    val name: String,
    val type: DeviceType,
    val host: String,
    val port: Int,
    val discriminator: Int,
    val passcode: Long,
    val serialNumber: String,
)
