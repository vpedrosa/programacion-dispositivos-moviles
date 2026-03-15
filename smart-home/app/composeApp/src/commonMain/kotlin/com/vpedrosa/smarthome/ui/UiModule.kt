package com.vpedrosa.smarthome.ui

import com.vpedrosa.smarthome.ui.screens.AntiSquatterViewModel
import com.vpedrosa.smarthome.ui.screens.CommissioningViewModel
import com.vpedrosa.smarthome.ui.screens.DashboardViewModel
import com.vpedrosa.smarthome.ui.screens.DeviceDetailViewModel
import com.vpedrosa.smarthome.ui.screens.DevicesViewModel
import com.vpedrosa.smarthome.ui.screens.EditGroupViewModel
import com.vpedrosa.smarthome.ui.screens.NotificationsViewModel
import com.vpedrosa.smarthome.ui.screens.RoomDetailViewModel
import com.vpedrosa.smarthome.ui.screens.RoomsViewModel
import com.vpedrosa.smarthome.ui.screens.SettingsViewModel
import com.vpedrosa.smarthome.ui.screens.VoiceControlViewModel
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
    viewModel { params -> DeviceDetailViewModel(params.get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { params -> EditGroupViewModel(params.getOrNull(), get(), get(), get()) }
}
