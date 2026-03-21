package com.vpedrosa.smarthome

import android.app.Application
import com.google.android.gms.wearable.Wearable
import com.vpedrosa.smarthome.di.deviceModule
import com.vpedrosa.smarthome.di.platformModule
import com.vpedrosa.smarthome.ui.uiModule
import com.vpedrosa.smarthome.wearable.WearableMessageHandler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SmartHomeApplication : Application() {

    private var wearableHandler: WearableMessageHandler? = null

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SmartHomeApplication)
            modules(platformModule, deviceModule, uiModule)
        }
        registerWearableListener()
    }

    /**
     * Registers a foreground MessageClient listener so that wearable
     * messages are handled even if the WearableListenerService is not
     * triggered by Play Services (e.g. on emulators).
     */
    private fun registerWearableListener() {
        try {
            val handler = WearableMessageHandler(this)
            Wearable.getMessageClient(this).addListener(handler)
            wearableHandler = handler
        } catch (_: Exception) {
            // Play Services not available
        }
    }
}
