package com.vpedrosa.smarthome.device

import com.vpedrosa.smarthome.device.adapters.discovery.StaticDeviceDiscoveryAdapter
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryAntiSquatterRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryAppSettingsRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryRoomRepository
import com.vpedrosa.smarthome.device.domain.ports.AntiSquatterRepository
import com.vpedrosa.smarthome.device.domain.ports.AppSettingsRepository
import com.vpedrosa.smarthome.device.domain.ports.DeviceDiscoveryPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceEventRepository
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import com.vpedrosa.smarthome.device.domain.ports.RoomRepository
import com.vpedrosa.smarthome.device.domain.usecases.BulkToggleDevicesByTypeInRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.domain.usecases.CommissionDeviceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ExecuteVoiceCommandUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ParseVoiceCommandUseCase
import com.vpedrosa.smarthome.device.domain.usecases.SimulatePresenceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ToggleDeviceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateBlindUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateLightUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateThermostatUseCase
import com.vpedrosa.smarthome.device.adapters.simulation.SensorEventSimulator
import com.vpedrosa.smarthome.device.domain.usecases.AddDeviceEventUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val deviceModule = module {
    // Driven ports (adapters)
    single<DeviceDiscoveryPort> { StaticDeviceDiscoveryAdapter() }
    single<DeviceRepository> { InMemoryDeviceRepository() }
    single<RoomRepository> { InMemoryRoomRepository() }
    single<DeviceEventRepository> { InMemoryDeviceEventRepository() }
    single<AntiSquatterRepository> { InMemoryAntiSquatterRepository() }
    single<AppSettingsRepository> { InMemoryAppSettingsRepository() }

    // Use cases
    factory { ToggleDeviceUseCase(get(), get()) }
    factory { UpdateLightUseCase(get(), get()) }
    factory { UpdateBlindUseCase(get(), get()) }
    factory { UpdateThermostatUseCase(get(), get()) }
    factory { BulkToggleDevicesByTypeUseCase(get(), get()) }
    factory { BulkToggleDevicesByTypeInRoomUseCase(get(), get(), get()) }
    factory { AddDeviceEventUseCase(get(), get()) }
    factory { SimulatePresenceUseCase(get(), get()) }
    factory { ParseVoiceCommandUseCase() }
    factory { CommissionDeviceUseCase(get(), get(), get()) }
    factory { ExecuteVoiceCommandUseCase(get(), get(), get()) }

    // Sensor event simulator (singleton, lazy start)
    single {
        SensorEventSimulator(
            addDeviceEvent = get(),
            deviceRepository = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }

}
