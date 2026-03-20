package com.vpedrosa.smarthome

import android.app.Application
import com.vpedrosa.smarthome.di.deviceModule
import com.vpedrosa.smarthome.di.platformModule
import com.vpedrosa.smarthome.ui.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SmartHomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SmartHomeApplication)
            modules(platformModule, deviceModule, uiModule)
        }
    }
}
