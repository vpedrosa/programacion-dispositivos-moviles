package com.vpedrosa.smarthome.wear.di

import com.vpedrosa.smarthome.wear.ui.device_control.DeviceControlViewModel
import com.vpedrosa.smarthome.wear.device_control.infrastructure.wearable.FakeDeviceCommandAdapter
import com.vpedrosa.smarthome.wear.device_control.infrastructure.wearable.WearableDeviceCommandAdapter
import com.vpedrosa.smarthome.wear.device_control.domain.DeviceCommandPort
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val wearModule = module {

    single<DeviceCommandPort> {
        val context = androidContext()
        try {
            WearableDeviceCommandAdapter(context)
        } catch (_: Exception) {
            FakeDeviceCommandAdapter()
        }
    }

    viewModel { DeviceControlViewModel(get()) }
}
