package com.vpedrosa.smarthome.wear.di

import android.speech.SpeechRecognizer
import com.vpedrosa.smarthome.wear.voice_control.VoiceControlViewModel
import com.vpedrosa.smarthome.wear.voice_control.adapters.FakeVoiceCommandAdapter
import com.vpedrosa.smarthome.wear.voice_control.adapters.FakeWearSpeechRecognizer
import com.vpedrosa.smarthome.wear.voice_control.adapters.WearSpeechRecognizerAdapter
import com.vpedrosa.smarthome.wear.voice_control.adapters.WearableVoiceCommandAdapter
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandPort
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.WearSpeechRecognizerPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val wearModule = module {

    // Speech recognizer: use real SpeechRecognizer if available, fake otherwise
    single<WearSpeechRecognizerPort> {
        val context = androidContext()
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            WearSpeechRecognizerAdapter(context)
        } else {
            FakeWearSpeechRecognizer(
                CoroutineScope(SupervisorJob() + Dispatchers.Main)
            )
        }
    }

    // Voice command port: use real Wearable Data Layer if Play Services available,
    // fall back to fake adapter for emulators without Play Services
    single<VoiceCommandPort> {
        val context = androidContext()
        try {
            // Try to instantiate the real adapter; it will fail at runtime
            // if Play Services is not available, but the MessageClient
            // gracefully handles missing nodes.
            WearableVoiceCommandAdapter(context)
        } catch (_: Exception) {
            FakeVoiceCommandAdapter()
        }
    }

    viewModel { VoiceControlViewModel(get(), get()) }
}
