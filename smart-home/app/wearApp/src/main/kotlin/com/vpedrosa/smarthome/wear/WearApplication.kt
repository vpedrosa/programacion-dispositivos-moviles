package com.vpedrosa.smarthome.wear

import android.app.Application
import com.vpedrosa.smarthome.wear.di.wearModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WearApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WearApplication)
            modules(wearModule)
        }
    }
}
