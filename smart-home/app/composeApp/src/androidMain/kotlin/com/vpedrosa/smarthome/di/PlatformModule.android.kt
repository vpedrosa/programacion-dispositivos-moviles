package com.vpedrosa.smarthome.di

import android.speech.SpeechRecognizer
import com.vpedrosa.smarthome.commissioning.domain.SimulatorDiscoveryPort
import com.vpedrosa.smarthome.commissioning.domain.SimulatorHostRepository
import com.vpedrosa.smarthome.commissioning.infrastructure.discovery.FallbackDeviceDiscoveryAdapter
import com.vpedrosa.smarthome.commissioning.infrastructure.discovery.LocalhostDeviceDiscoveryAdapter
import com.vpedrosa.smarthome.commissioning.infrastructure.discovery.MdnsDeviceDiscoveryAdapter
import com.vpedrosa.smarthome.commissioning.infrastructure.discovery.MdnsSimulatorDiscoveryAdapter
import com.vpedrosa.smarthome.commissioning.infrastructure.persistence.InMemorySimulatorHostRepository
import com.vpedrosa.smarthome.commissioning.infrastructure.matter.MatterCommissioningAdapter
import com.vpedrosa.smarthome.device.infrastructure.matter.MatterControllerProvider
import com.vpedrosa.smarthome.device.infrastructure.matter.MatterDeviceControlAdapter
import com.vpedrosa.smarthome.event.infrastructure.AndroidNotificationAdapter
import com.vpedrosa.smarthome.voice.infrastructure.speech.AndroidSpeechRecognizerAdapter
import com.vpedrosa.smarthome.voice.infrastructure.speech.FakeSpeechRecognizer
import com.vpedrosa.smarthome.commissioning.domain.CommissioningPort
import com.vpedrosa.smarthome.commissioning.domain.DeviceDiscoveryPort
import com.vpedrosa.smarthome.device.domain.DeviceControlPort
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
    single<DeviceControlPort> { MatterDeviceControlAdapter(get<MatterControllerProvider>().controller) }
    single<NotificationPort> { AndroidNotificationAdapter(androidContext()) }
    single<SimulatorHostRepository> { InMemorySimulatorHostRepository() }
    single<SimulatorDiscoveryPort> { MdnsSimulatorDiscoveryAdapter(androidContext()) }

    // Commissioning: PASE over network (works for both emulator and simulated hub)
    single<CommissioningPort> {
        val controller = get<MatterControllerProvider>().controller
        MatterCommissioningAdapter(controller)
    }

    // Discovery: mDNS en dispositivo físico, fallback a localhost en emulador
    single<DeviceDiscoveryPort> {
        FallbackDeviceDiscoveryAdapter(
            primary = MdnsDeviceDiscoveryAdapter(androidContext()),
            fallback = LocalhostDeviceDiscoveryAdapter(),
            fallbackDelayMs = 10_000L,
        )
    }

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
