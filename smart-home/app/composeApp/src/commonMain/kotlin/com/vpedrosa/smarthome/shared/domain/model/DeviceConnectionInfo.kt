package com.vpedrosa.smarthome.shared.domain.model

data class DeviceConnectionInfo(
    val host: String,
    val port: Int,
    val passcode: Long,
)
