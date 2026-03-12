package com.vpedrosa.smarthome.device.domain

data class DiscoveredDevice(
    val name: String,
    val type: DeviceType,
    val host: String,
    val port: Int,
    val discriminator: Int,
    val passcode: Long,
    val serialNumber: String,
)
