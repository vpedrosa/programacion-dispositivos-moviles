package com.vpedrosa.smarthome

import android.app.Application
import com.vpedrosa.smarthome.device.deviceModule
import com.vpedrosa.smarthome.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SmartHomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SmartHomeApplication)
            modules(deviceModule, platformModule)
        }
    }
}
