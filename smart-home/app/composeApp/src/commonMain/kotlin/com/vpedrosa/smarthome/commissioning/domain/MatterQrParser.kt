package com.vpedrosa.smarthome.commissioning.domain

data class MatterQrPayload(
    val discriminator: Int,
    val passcode: Long,
)

expect fun parseMatterQrCode(qrCode: String): MatterQrPayload?
