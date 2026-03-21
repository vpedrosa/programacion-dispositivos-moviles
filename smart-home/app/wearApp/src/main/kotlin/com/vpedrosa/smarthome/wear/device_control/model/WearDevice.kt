package com.vpedrosa.smarthome.wear.device_control.model

import kotlinx.serialization.Serializable

@Serializable
data class WearDevice(
    val id: String,
    val name: String,
    val type: String,
    val roomName: String?,
    val isOn: Boolean = false,
    val isLocked: Boolean = false,
    val openingLevel: Int = 0,
    val currentTemperature: Double = 0.0,
    val targetTemperature: Double = 0.0,
    val isHeatingOn: Boolean = false,
    val isSmokeDetected: Boolean = false,
    val isLeakDetected: Boolean = false,
    val isContactOpen: Boolean = false,
)

@Serializable
data class WearDeviceList(
    val devices: List<WearDevice>,
)

@Serializable
data class WearDeviceAction(
    val deviceId: String,
    val action: String,
) {
    companion object {
        const val TOGGLE = "TOGGLE"
    }
}
