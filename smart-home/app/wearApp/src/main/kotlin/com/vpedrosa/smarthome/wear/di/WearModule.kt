package com.vpedrosa.smarthome.wear.di

import com.vpedrosa.smarthome.wear.voice_control.VoiceControlViewModel
import com.vpedrosa.smarthome.wear.voice_control.adapters.FakeVoiceCommandAdapter
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandPort
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val wearModule = module {
    single<VoiceCommandPort> { FakeVoiceCommandAdapter() }
    viewModel { VoiceControlViewModel(get()) }
}
