package com.vpedrosa.smarthome.device.domain.model

data class DeviceConnectionInfo(
    val host: String,
    val port: Int,
    val passcode: Long,
)
