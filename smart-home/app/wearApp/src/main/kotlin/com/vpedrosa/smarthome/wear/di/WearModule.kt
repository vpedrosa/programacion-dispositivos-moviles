package com.vpedrosa.smarthome.wear.di

import com.vpedrosa.smarthome.wear.device_control.DeviceControlViewModel
import com.vpedrosa.smarthome.wear.device_control.adapters.FakeDeviceCommandAdapter
import com.vpedrosa.smarthome.wear.device_control.adapters.WearableDeviceCommandAdapter
import com.vpedrosa.smarthome.wear.device_control.domain.ports.DeviceCommandPort
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
