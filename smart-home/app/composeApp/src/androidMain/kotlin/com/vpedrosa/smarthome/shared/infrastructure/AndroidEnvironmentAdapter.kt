package com.vpedrosa.smarthome.shared.infrastructure

import android.os.Build
import com.vpedrosa.smarthome.shared.domain.EnvironmentPort

/**
 * Detects whether the app is running on an Android emulator.
 *
 * Uses multiple [Build] properties for reliable detection across
 * different emulator types (AVD, Genymotion, etc.).
 */
class AndroidEnvironmentAdapter : EnvironmentPort {

    override val isEmulator: Boolean by lazy {
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            Build.BRAND.startsWith("generic") ||
            Build.DEVICE.startsWith("generic") ||
            Build.PRODUCT.contains("sdk") ||
            Build.PRODUCT.contains("emulator") ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu")
    }
}
