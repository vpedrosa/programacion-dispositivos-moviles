package com.vpedrosa.smarthome.wear.di

import com.vpedrosa.smarthome.wear.voice_control.VoiceControlViewModel
import com.vpedrosa.smarthome.wear.voice_control.adapters.FakeVoiceCommandAdapter
import com.vpedrosa.smarthome.wear.voice_control.adapters.WearableVoiceCommandAdapter
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandPort
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val wearModule = module {

    // Voice command port: use real Wearable Data Layer if Play Services available,
    // fall back to fake adapter for emulators without Play Services
    single<VoiceCommandPort> {
        val context = androidContext()
        try {
            WearableVoiceCommandAdapter(context)
        } catch (_: Exception) {
            FakeVoiceCommandAdapter()
        }
    }

    viewModel { VoiceControlViewModel(get()) }
}
