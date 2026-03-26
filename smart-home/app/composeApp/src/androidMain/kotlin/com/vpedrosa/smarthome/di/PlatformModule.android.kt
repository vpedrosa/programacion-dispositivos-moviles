package com.vpedrosa.smarthome.di

import android.speech.SpeechRecognizer
import com.vpedrosa.smarthome.commissioning.domain.SimulatorDiscoveryPort
import com.vpedrosa.smarthome.commissioning.domain.SimulatorHostRepository
import com.vpedrosa.smarthome.commissioning.infrastructure.discovery.MdnsDeviceDiscoveryAdapter
import com.vpedrosa.smarthome.commissioning.infrastructure.discovery.MdnsSimulatorDiscoveryAdapter
import com.vpedrosa.smarthome.commissioning.infrastructure.discovery.StaticDeviceDiscoveryAdapter
import com.vpedrosa.smarthome.commissioning.infrastructure.persistence.InMemorySimulatorHostRepository
import com.vpedrosa.smarthome.commissioning.infrastructure.matter.MatterCommissioningAdapter
import com.vpedrosa.smarthome.commissioning.infrastructure.matter.ProductionCommissioningAdapter
import com.vpedrosa.smarthome.shared.infrastructure.matter.MatterControllerProvider
import com.vpedrosa.smarthome.shared.infrastructure.matter.MatterDeviceControlAdapter
import com.vpedrosa.smarthome.event.infrastructure.notification.AndroidNotificationAdapter
import com.vpedrosa.smarthome.voice.infrastructure.speech.AndroidSpeechRecognizerAdapter
import com.vpedrosa.smarthome.voice.infrastructure.speech.FakeSpeechRecognizer
import com.vpedrosa.smarthome.commissioning.domain.CommissioningPort
import com.vpedrosa.smarthome.commissioning.domain.DeviceDiscoveryPort
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.shared.domain.EnvironmentPort
import com.vpedrosa.smarthome.shared.infrastructure.AndroidEnvironmentAdapter
import com.vpedrosa.smarthome.event.domain.NotificationPort
import com.vpedrosa.smarthome.voice.domain.SpeechRecognizerPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<EnvironmentPort> { AndroidEnvironmentAdapter() }
    single { MatterControllerProvider(androidContext()) }
    single<DeviceControlPort> { MatterDeviceControlAdapter(get<MatterControllerProvider>().controller) }
    single<NotificationPort> { AndroidNotificationAdapter(androidContext()) }
    single<SimulatorHostRepository> { InMemorySimulatorHostRepository() }
    single<SimulatorDiscoveryPort> { MdnsSimulatorDiscoveryAdapter(androidContext()) }

    // Commissioning: PASE for emulator, BLE+mDNS for production
    single<CommissioningPort> {
        val env = get<EnvironmentPort>()
        val controller = get<MatterControllerProvider>().controller
        if (env.isEmulator) {
            MatterCommissioningAdapter(controller)
        } else {
            ProductionCommissioningAdapter(controller)
        }
    }

    // Discovery: static list for emulator, mDNS for production
    single<DeviceDiscoveryPort> {
        val env = get<EnvironmentPort>()
        if (env.isEmulator) {
            StaticDeviceDiscoveryAdapter(get())
        } else {
            MdnsDeviceDiscoveryAdapter(androidContext())
        }
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
