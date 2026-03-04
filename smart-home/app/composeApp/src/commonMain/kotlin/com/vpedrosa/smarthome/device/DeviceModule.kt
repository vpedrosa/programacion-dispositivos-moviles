package com.vpedrosa.smarthome.device

import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryRoomRepository
import com.vpedrosa.smarthome.device.domain.ports.DeviceEventRepository
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import com.vpedrosa.smarthome.device.domain.ports.RoomRepository
import com.vpedrosa.smarthome.device.domain.usecases.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllDevicesUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllRoomsUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDeviceEventsUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDeviceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDevicesByRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ToggleCastingUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ToggleDeviceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateBlindUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateLightUseCase
import com.vpedrosa.smarthome.device.domain.usecases.DeleteRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.SaveRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateThermostatUseCase
import com.vpedrosa.smarthome.device.domain.SensorEventSimulator
import com.vpedrosa.smarthome.device.domain.usecases.AddDeviceEventUseCase
import com.vpedrosa.smarthome.ui.screens.DashboardViewModel
import com.vpedrosa.smarthome.ui.screens.DeviceDetailViewModel
import com.vpedrosa.smarthome.ui.screens.DevicesViewModel
import com.vpedrosa.smarthome.ui.screens.EditGroupViewModel
import com.vpedrosa.smarthome.ui.screens.NotificationsViewModel
import com.vpedrosa.smarthome.ui.screens.RoomsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val deviceModule = module {
    // Driven ports (adapters)
    single<DeviceRepository> { InMemoryDeviceRepository() }
    single<RoomRepository> { InMemoryRoomRepository() }
    single<DeviceEventRepository> { InMemoryDeviceEventRepository() }

    // Use cases
    factory { ObserveAllDevicesUseCase(get()) }
    factory { ObserveDeviceUseCase(get()) }
    factory { ObserveDevicesByRoomUseCase(get()) }
    factory { ObserveDevicesByTypeUseCase(get()) }
    factory { ToggleDeviceUseCase(get()) }
    factory { UpdateLightUseCase(get()) }
    factory { UpdateBlindUseCase(get()) }
    factory { UpdateThermostatUseCase(get()) }
    factory { ToggleCastingUseCase(get()) }
    factory { ObserveAllRoomsUseCase(get()) }
    factory { ObserveRoomUseCase(get()) }
    factory { ObserveDeviceEventsUseCase(get()) }
    factory { BulkToggleDevicesByTypeUseCase(get()) }
    factory { SaveRoomUseCase(get()) }
    factory { DeleteRoomUseCase(get()) }
    factory { AddDeviceEventUseCase(get()) }

    // Sensor event simulator (singleton, lazy start)
    single {
        SensorEventSimulator(
            addDeviceEvent = get(),
            observeAllDevices = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }

    // ViewModels
    viewModelOf(::DevicesViewModel)
    viewModelOf(::RoomsViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::NotificationsViewModel)
    viewModel { params -> DeviceDetailViewModel(params.get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { params -> EditGroupViewModel(params.getOrNull(), get(), get(), get()) }
}
