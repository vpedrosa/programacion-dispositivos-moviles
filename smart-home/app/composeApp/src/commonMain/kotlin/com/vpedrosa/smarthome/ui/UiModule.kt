package com.vpedrosa.smarthome.ui

import com.vpedrosa.smarthome.ui.antisquatter.AntiSquatterViewModel
import com.vpedrosa.smarthome.ui.commissioning.CommissioningViewModel
import com.vpedrosa.smarthome.ui.dashboard.DashboardViewModel
import com.vpedrosa.smarthome.ui.device.DeviceDetailViewModel
import com.vpedrosa.smarthome.ui.device.DevicesViewModel
import com.vpedrosa.smarthome.ui.room.EditGroupViewModel
import com.vpedrosa.smarthome.ui.event.NotificationsViewModel
import com.vpedrosa.smarthome.ui.room.RoomDetailViewModel
import com.vpedrosa.smarthome.ui.room.RoomsViewModel
import com.vpedrosa.smarthome.ui.settings.SettingsViewModel
import com.vpedrosa.smarthome.ui.voice.VoiceControlViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    viewModelOf(::CommissioningViewModel)
    viewModelOf(::DevicesViewModel)
    viewModelOf(::RoomsViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::NotificationsViewModel)
    viewModelOf(::AntiSquatterViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::VoiceControlViewModel)
    viewModel { params -> RoomDetailViewModel(params.get(), get(), get(), get(), get()) }
    viewModel { params -> DeviceDetailViewModel(params.get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { params -> EditGroupViewModel(params.getOrNull(), get(), get(), get()) }
}
