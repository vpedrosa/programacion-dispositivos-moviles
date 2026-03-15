package com.vpedrosa.smarthome.device

import com.vpedrosa.smarthome.device.adapters.discovery.StaticDeviceDiscoveryAdapter
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryAntiSquatterRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryAppSettingsRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.device.adapters.persistence.InMemoryRoomRepository
import com.vpedrosa.smarthome.device.adapters.speech.FakeSpeechRecognizer
import com.vpedrosa.smarthome.device.domain.ports.AntiSquatterRepository
import com.vpedrosa.smarthome.device.domain.ports.AppSettingsRepository
import com.vpedrosa.smarthome.device.domain.ports.DeviceDiscoveryPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceEventRepository
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import com.vpedrosa.smarthome.device.domain.ports.RoomRepository
import com.vpedrosa.smarthome.device.domain.ports.SpeechRecognizerPort
import com.vpedrosa.smarthome.device.domain.usecases.BulkToggleDevicesByTypeInRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.domain.usecases.CommissionDeviceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ExecuteVoiceCommandUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllDevicesUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllRoomsUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAntiSquatterConfigUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAppSettingsUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDeviceEventsUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDeviceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDevicesByRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDiscoveredDevicesUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ParseVoiceCommandUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.SaveAntiSquatterConfigUseCase
import com.vpedrosa.smarthome.device.domain.usecases.SaveAppSettingsUseCase
import com.vpedrosa.smarthome.device.domain.usecases.SaveRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.SimulatePresenceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ToggleCastingUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ToggleDeviceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateBlindUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateLightUseCase
import com.vpedrosa.smarthome.device.domain.usecases.DeleteRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateThermostatUseCase
import com.vpedrosa.smarthome.device.domain.SensorEventSimulator
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
    single<SpeechRecognizerPort> {
        FakeSpeechRecognizer(CoroutineScope(SupervisorJob() + Dispatchers.Default))
    }

    // Use cases
    factory { ObserveAllDevicesUseCase(get()) }
    factory { ObserveDeviceUseCase(get()) }
    factory { ObserveDevicesByRoomUseCase(get()) }
    factory { ObserveDevicesByTypeUseCase(get()) }
    factory { ToggleDeviceUseCase(get(), get()) }
    factory { UpdateLightUseCase(get(), get()) }
    factory { UpdateBlindUseCase(get(), get()) }
    factory { UpdateThermostatUseCase(get(), get()) }
    factory { ToggleCastingUseCase(get()) }
    factory { ObserveAllRoomsUseCase(get()) }
    factory { ObserveRoomUseCase(get()) }
    factory { ObserveDeviceEventsUseCase(get()) }
    factory { BulkToggleDevicesByTypeUseCase(get(), get()) }
    factory { BulkToggleDevicesByTypeInRoomUseCase(get(), get(), get()) }
    factory { SaveRoomUseCase(get()) }
    factory { DeleteRoomUseCase(get()) }
    factory { AddDeviceEventUseCase(get(), get()) }
    factory { ObserveAntiSquatterConfigUseCase(get()) }
    factory { SaveAntiSquatterConfigUseCase(get()) }
    factory { SimulatePresenceUseCase(get(), get()) }
    factory { ObserveAppSettingsUseCase(get()) }
    factory { SaveAppSettingsUseCase(get()) }
    factory { ParseVoiceCommandUseCase() }
    factory { ObserveDiscoveredDevicesUseCase(get()) }
    factory { CommissionDeviceUseCase(get(), get(), get()) }
    factory { ExecuteVoiceCommandUseCase(get(), get(), get()) }

    // Sensor event simulator (singleton, lazy start)
    single {
        SensorEventSimulator(
            addDeviceEvent = get(),
            observeAllDevices = get(),
            deviceRepository = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }

}
