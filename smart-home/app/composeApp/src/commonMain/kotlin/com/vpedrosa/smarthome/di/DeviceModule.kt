package com.vpedrosa.smarthome.di

import com.vpedrosa.smarthome.commissioning.infrastructure.discovery.StaticDeviceDiscoveryAdapter
import com.vpedrosa.smarthome.antisquatter.infrastructure.persistence.InMemoryAntiSquatterRepository
import com.vpedrosa.smarthome.settings.infrastructure.persistence.InMemoryAppSettingsRepository
import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryRoomRepository
import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import com.vpedrosa.smarthome.settings.domain.AppSettingsRepository
import com.vpedrosa.smarthome.commissioning.domain.DeviceDiscoveryPort
import com.vpedrosa.smarthome.shared.domain.DeviceEventRepository
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeInRoomUseCase
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.application.GetAllDevicesWithRoomUseCase
import com.vpedrosa.smarthome.commissioning.application.CommissionDeviceUseCase
import com.vpedrosa.smarthome.voice.application.ExecuteVoiceCommandUseCase
import com.vpedrosa.smarthome.voice.application.ParseVoiceCommandUseCase
import com.vpedrosa.smarthome.voice.domain.VoiceCommandRepository
import com.vpedrosa.smarthome.voice.infrastructure.persistence.InMemoryVoiceCommandRepository
import com.vpedrosa.smarthome.antisquatter.application.SimulatePresenceUseCase
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import com.vpedrosa.smarthome.device.application.UpdateBlindUseCase
import com.vpedrosa.smarthome.device.application.UpdateLightUseCase
import com.vpedrosa.smarthome.device.application.UpdateThermostatUseCase
import com.vpedrosa.smarthome.event.infrastructure.simulation.SensorEventSimulator
import com.vpedrosa.smarthome.event.application.AddDeviceEventUseCase
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
    single<VoiceCommandRepository> { InMemoryVoiceCommandRepository() }

    // Use cases
    factory { GetAllDevicesWithRoomUseCase(get(), get()) }
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
