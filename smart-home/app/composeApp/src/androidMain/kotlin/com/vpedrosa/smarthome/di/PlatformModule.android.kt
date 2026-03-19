package com.vpedrosa.smarthome.di

import android.speech.SpeechRecognizer
import com.vpedrosa.smarthome.commissioning.infrastructure.matter.MatterCommissioningAdapter
import com.vpedrosa.smarthome.shared.infrastructure.matter.MatterControllerProvider
import com.vpedrosa.smarthome.shared.infrastructure.matter.MatterDeviceControlAdapter
import com.vpedrosa.smarthome.event.infrastructure.notification.AndroidNotificationAdapter
import com.vpedrosa.smarthome.voice.infrastructure.speech.AndroidSpeechRecognizerAdapter
import com.vpedrosa.smarthome.voice.infrastructure.speech.FakeSpeechRecognizer
import com.vpedrosa.smarthome.commissioning.domain.CommissioningPort
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.event.domain.NotificationPort
import com.vpedrosa.smarthome.voice.domain.SpeechRecognizerPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { MatterControllerProvider(androidContext()) }
    single<CommissioningPort> { MatterCommissioningAdapter(get<MatterControllerProvider>().controller) }
    single<DeviceControlPort> { MatterDeviceControlAdapter(get<MatterControllerProvider>().controller) }
    single<NotificationPort> { AndroidNotificationAdapter(androidContext()) }

    // Speech recognizer: use real Android adapter when available, fall back to fake
    single<SpeechRecognizerPort> {
        val context = androidContext()
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            AndroidSpeechRecognizerAdapter(context)
        } else {
            FakeSpeechRecognizer(CoroutineScope(SupervisorJob() + Dispatchers.Default))
        }
    }
}
