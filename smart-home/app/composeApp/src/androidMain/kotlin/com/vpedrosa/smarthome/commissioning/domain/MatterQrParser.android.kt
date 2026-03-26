package com.vpedrosa.smarthome.commissioning.domain

import android.util.Log
import chip.setuppayload.SetupPayloadParser

actual fun parseMatterQrCode(qrCode: String): MatterQrPayload? {
    return try {
        val parser = SetupPayloadParser()
        val payload = parser.parseQrCode(qrCode)
        MatterQrPayload(
            discriminator = payload.discriminator,
            passcode = payload.setupPinCode,
        )
    } catch (e: Exception) {
        Log.e("MatterQrParser", "Failed to parse QR code: $qrCode", e)
        null
    }
}
