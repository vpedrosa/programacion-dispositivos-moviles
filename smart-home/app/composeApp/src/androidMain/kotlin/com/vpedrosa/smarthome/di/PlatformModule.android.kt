package com.vpedrosa.smarthome.di

import com.vpedrosa.smarthome.device.adapters.matter.MatterCommissioningAdapter
import com.vpedrosa.smarthome.device.domain.ports.CommissioningPort
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<CommissioningPort> { MatterCommissioningAdapter(get()) }
}
