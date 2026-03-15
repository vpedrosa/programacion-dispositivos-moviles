package com.vpedrosa.smarthome.di

import com.vpedrosa.smarthome.device.deviceModule
import com.vpedrosa.smarthome.ui.uiModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(): KoinApplication = startKoin {
    modules(platformModule, deviceModule, uiModule)
}
