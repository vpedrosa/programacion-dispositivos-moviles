package com.vpedrosa.smarthome.shared.domain

/**
 * Port to detect the runtime environment.
 *
 * In emulator mode, the app uses PASE-based commissioning against
 * the matter.js simulator. In production mode, the app uses the
 * full BLE + mDNS commissioning flow for real Matter devices.
 */
interface EnvironmentPort {
    val isEmulator: Boolean
    val isProduction: Boolean get() = !isEmulator
}
