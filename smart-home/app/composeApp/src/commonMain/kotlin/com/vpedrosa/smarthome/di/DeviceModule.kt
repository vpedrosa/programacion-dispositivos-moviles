package com.vpedrosa.smarthome.di

import com.vpedrosa.smarthome.antisquatter.infrastructure.InMemoryAntiSquatterRepository
import com.vpedrosa.smarthome.settings.infrastructure.InMemoryAppSettingsRepository
import com.vpedrosa.smarthome.device.infrastructure.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.device.infrastructure.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.room.infrastructure.InMemoryRoomRepository
import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import com.vpedrosa.smarthome.settings.domain.AppSettingsRepository
import com.vpedrosa.smarthome.device.domain.DeviceEventRepository
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import com.vpedrosa.smarthome.room.domain.RoomRepository
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeInRoomUseCase
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.application.DeregisterDeviceUseCase
import com.vpedrosa.smarthome.device.application.GetAllDevicesWithRoomUseCase
import com.vpedrosa.smarthome.device.application.LaunchContentUseCase
import com.vpedrosa.smarthome.device.application.LockDoorUseCase
import com.vpedrosa.smarthome.room.application.DeleteRoomUseCase
import com.vpedrosa.smarthome.room.application.SaveRoomUseCase
import com.vpedrosa.smarthome.commissioning.application.ClearSimulatorHostUseCase
import com.vpedrosa.smarthome.commissioning.application.CommissionDeviceUseCase
import com.vpedrosa.smarthome.commissioning.application.GetDiscoveredDevicesUseCase
import com.vpedrosa.smarthome.commissioning.application.SearchSimulatorUseCase
import com.vpedrosa.smarthome.settings.application.ToggleNotificationsUseCase
import com.vpedrosa.smarthome.voice.application.ExecuteVoiceCommandUseCase
import com.vpedrosa.smarthome.voice.application.ParseVoiceCommandUseCase
import com.vpedrosa.smarthome.voice.domain.VoiceCommandRepository
import com.vpedrosa.smarthome.voice.infrastructure.persistence.InMemoryVoiceCommandRepository
import com.vpedrosa.smarthome.antisquatter.application.SimulatePresenceUseCase
import com.vpedrosa.smarthome.antisquatter.application.ToggleAntiSquatterUseCase
import com.vpedrosa.smarthome.antisquatter.application.UpdateAntiSquatterActionDurationUseCase
import com.vpedrosa.smarthome.antisquatter.application.UpdateAntiSquatterEndTimeUseCase
import com.vpedrosa.smarthome.antisquatter.application.UpdateAntiSquatterStartTimeUseCase
import com.vpedrosa.smarthome.commissioning.application.FindDiscoveredDeviceByQrUseCase
import com.vpedrosa.smarthome.voice.application.RecordVoiceCommandUseCase
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import com.vpedrosa.smarthome.device.application.UpdateBlindUseCase
import com.vpedrosa.smarthome.device.application.UpdateLightUseCase
import com.vpedrosa.smarthome.device.application.UpdateThermostatUseCase
import com.vpedrosa.smarthome.event.domain.BackgroundSimulatorPort
import com.vpedrosa.smarthome.event.infrastructure.SensorEventSimulator
import com.vpedrosa.smarthome.event.application.AddDeviceEventUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val deviceModule = module {
    // Driven ports (adapters)
    single<DeviceRepository> { InMemoryDeviceRepository() }
    single<RoomRepository> { InMemoryRoomRepository() }
    single<DeviceEventRepository> { InMemoryDeviceEventRepository() }
    single<AntiSquatterRepository> { InMemoryAntiSquatterRepository() }
    single<AppSettingsRepository> { InMemoryAppSettingsRepository() }
    single<VoiceCommandRepository> { InMemoryVoiceCommandRepository() }

    // Use cases
    factory { GetAllDevicesWithRoomUseCase(get(), get()) }
    factory { DeregisterDeviceUseCase(get(), get()) }
    factory { LaunchContentUseCase(get(), get()) }
    factory { SaveRoomUseCase(get()) }
    factory { DeleteRoomUseCase(get()) }
    factory { GetDiscoveredDevicesUseCase(get()) }
    factory { ToggleDeviceUseCase(get(), get(), get()) }
    factory { UpdateLightUseCase(get(), get()) }
    factory { UpdateBlindUseCase(get(), get()) }
    factory { UpdateThermostatUseCase(get(), get()) }
    factory { BulkToggleDevicesByTypeUseCase(get(), get()) }
    factory { BulkToggleDevicesByTypeInRoomUseCase(get(), get(), get()) }
    factory { AddDeviceEventUseCase(get(), get(), get()) }
    factory { SimulatePresenceUseCase(get(), get(), get(), get()) }
    factory { ParseVoiceCommandUseCase() }
    factory { CommissionDeviceUseCase(get(), get(), get()) }
    factory { LockDoorUseCase(get(), get()) }
    factory { ExecuteVoiceCommandUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { ToggleNotificationsUseCase(get()) }
    factory { SearchSimulatorUseCase(get(), get()) }
    factory { ClearSimulatorHostUseCase(get()) }
    factory { ToggleAntiSquatterUseCase(get()) }
    factory { UpdateAntiSquatterStartTimeUseCase(get()) }
    factory { UpdateAntiSquatterEndTimeUseCase(get()) }
    factory { UpdateAntiSquatterActionDurationUseCase(get()) }
    factory { FindDiscoveredDeviceByQrUseCase() }
    factory { RecordVoiceCommandUseCase(get()) }

    // Sensor event simulator (singleton, lazy start)
    single<BackgroundSimulatorPort> {
        SensorEventSimulator(
            addDeviceEvent = get(),
            deviceRepository = get(),
            simulatePresence = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }

}
