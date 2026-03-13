package com.vpedrosa.smarthome.device.adapters.matter

import android.content.Context
import chip.devicecontroller.ChipDeviceController
import chip.devicecontroller.ControllerParams
import chip.platform.AndroidBleManager
import chip.platform.AndroidChipPlatform
import chip.platform.ChipMdnsCallbackImpl
import chip.platform.DiagnosticDataProviderImpl
import chip.platform.NsdManagerServiceBrowser
import chip.platform.NsdManagerServiceResolver
import chip.platform.PreferencesConfigurationManager
import chip.platform.PreferencesKeyValueStoreManager

class MatterControllerProvider(context: Context) {

    val controller: ChipDeviceController

    init {
        System.loadLibrary("CHIPController")

        AndroidChipPlatform(
            AndroidBleManager(),
            PreferencesKeyValueStoreManager(context),
            PreferencesConfigurationManager(context),
            NsdManagerServiceResolver(context),
            NsdManagerServiceBrowser(context),
            ChipMdnsCallbackImpl(),
            DiagnosticDataProviderImpl(context),
        )

        controller = ChipDeviceController(
            ControllerParams.newBuilder()
                .setUdpListenPort(0)
                .setControllerVendorId(VENDOR_ID)
                .build(),
        )
    }

    private companion object {
        const val VENDOR_ID = 0xFFF1
    }
}
