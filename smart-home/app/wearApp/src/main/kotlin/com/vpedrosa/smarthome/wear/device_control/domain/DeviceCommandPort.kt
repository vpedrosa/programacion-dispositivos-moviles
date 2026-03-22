package com.vpedrosa.smarthome.wear.device_control.domain

import com.vpedrosa.smarthome.wear.device_control.domain.model.WearDevice

/**
 * Driven port for communicating with the phone app to fetch
 * device data and execute actions via the Wearable Data Layer API.
 */
interface DeviceCommandPort {

    /**
     * Requests the current list of devices from the phone app.
     */
    suspend fun requestDeviceList(): DeviceListResult

    /**
     * Sends a toggle action for the given device to the phone app.
     * Returns the updated device on success.
     */
    suspend fun sendToggleAction(deviceId: String): ActionResult
}

sealed interface DeviceListResult {
    data class Success(val devices: List<WearDevice>) : DeviceListResult
    data class Error(val message: String) : DeviceListResult
}

sealed interface ActionResult {
    data class Success(val updatedDevice: WearDevice) : ActionResult
    data class Error(val message: String) : ActionResult
}
