package com.vpedrosa.smarthome.wear.device_control.infrastructure.wearable

import android.util.Log
import com.vpedrosa.smarthome.wear.device_control.domain.ActionResult
import com.vpedrosa.smarthome.wear.device_control.domain.DeviceCommandPort
import com.vpedrosa.smarthome.wear.device_control.domain.DeviceListResult

/**
 * Decorator that delegates to the [primary] adapter and falls back to
 * a [FakeDeviceCommandAdapter] if the primary fails (e.g. on emulators
 * where the Wearable Data Layer API doesn't deliver messages).
 */
class FallbackDeviceCommandAdapter(
    private val primary: DeviceCommandPort,
) : DeviceCommandPort {

    private var activePort: DeviceCommandPort = primary
    private var fallbackAttempted = false

    override suspend fun requestDeviceList(): DeviceListResult {
        val result = activePort.requestDeviceList()
        if (result is DeviceListResult.Error && !fallbackAttempted) {
            Log.w(TAG, "Primary adapter failed: ${result.message}. Falling back to local data.")
            fallbackAttempted = true
            activePort = FakeDeviceCommandAdapter()
            return activePort.requestDeviceList()
        }
        return result
    }

    override suspend fun sendToggleAction(deviceId: String): ActionResult {
        return activePort.sendToggleAction(deviceId)
    }

    private companion object {
        const val TAG = "FallbackDeviceCmd"
    }
}
