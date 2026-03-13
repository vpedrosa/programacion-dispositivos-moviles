package com.vpedrosa.smarthome.di

import com.vpedrosa.smarthome.device.adapters.matter.MatterCommissioningAdapter
import com.vpedrosa.smarthome.device.adapters.matter.MatterControllerProvider
import com.vpedrosa.smarthome.device.adapters.matter.MatterDeviceControlAdapter
import com.vpedrosa.smarthome.device.domain.ports.CommissioningPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceControlPort
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { MatterControllerProvider(androidContext()) }
    single<CommissioningPort> { MatterCommissioningAdapter(get<MatterControllerProvider>().controller) }
    single<DeviceControlPort> { MatterDeviceControlAdapter(get<MatterControllerProvider>().controller) }
}
