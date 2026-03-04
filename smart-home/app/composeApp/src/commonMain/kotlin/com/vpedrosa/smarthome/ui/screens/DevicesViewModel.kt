package com.vpedrosa.smarthome.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.Blind
import com.vpedrosa.smarthome.device.domain.ContactSensor
import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.DeviceType
import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.Lock
import com.vpedrosa.smarthome.device.domain.Room
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.SmartTv
import com.vpedrosa.smarthome.device.domain.SmokeSensor
import com.vpedrosa.smarthome.device.domain.Switch
import com.vpedrosa.smarthome.device.domain.TemperatureSensor
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.WaterLeakSensor
import com.vpedrosa.smarthome.device.domain.usecases.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllDevicesUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllRoomsUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ToggleDeviceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DevicesUiState(
    val devicesByType: Map<DeviceType, List<Device>> = emptyMap(),
    val roomNames: Map<String, String> = emptyMap(),
)

class DevicesViewModel(
    observeAllDevices: ObserveAllDevicesUseCase,
    observeAllRooms: ObserveAllRoomsUseCase,
    private val toggleDevice: ToggleDeviceUseCase,
    private val bulkToggleDevicesByType: BulkToggleDevicesByTypeUseCase,
) : ViewModel() {

    val uiState: StateFlow<DevicesUiState> = combine(
        observeAllDevices(),
        observeAllRooms(),
    ) { devices, rooms ->
        val grouped = devices
            .groupBy { it.type }
            .toSortedMap(compareBy { it.ordinal })

        val roomNamesMap = rooms.associate { it.id.value to it.name }

        DevicesUiState(
            devicesByType = grouped,
            roomNames = roomNamesMap,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DevicesUiState(),
    )

    fun onToggleDevice(deviceId: DeviceId) {
        viewModelScope.launch {
            toggleDevice(deviceId)
        }
    }

    fun onBulkToggle(type: DeviceType, turnOn: Boolean) {
        viewModelScope.launch {
            bulkToggleDevicesByType(type, turnOn)
        }
    }
}

/**
 * Whether this device type supports toggling (on/off or lock/unlock).
 * Sensors and blinds do not have a toggle state.
 */
fun DeviceType.isToggleable(): Boolean = when (this) {
    DeviceType.LIGHT,
    DeviceType.LOCK,
    DeviceType.SWITCH,
    DeviceType.SMART_TV,
    DeviceType.THERMOSTAT,
    DeviceType.CONTACT_SENSOR -> true

    DeviceType.BLIND,
    DeviceType.SMOKE_SENSOR,
    DeviceType.WATER_LEAK_SENSOR,
    DeviceType.TEMPERATURE_SENSOR -> false
}

/**
 * Whether this device type supports bulk toggle actions.
 * Contact sensors are individually toggleable but not bulk-toggled.
 */
fun DeviceType.supportsBulkToggle(): Boolean = when (this) {
    DeviceType.LIGHT,
    DeviceType.LOCK,
    DeviceType.SWITCH,
    DeviceType.SMART_TV,
    DeviceType.THERMOSTAT -> true

    DeviceType.BLIND,
    DeviceType.SMOKE_SENSOR,
    DeviceType.WATER_LEAK_SENSOR,
    DeviceType.TEMPERATURE_SENSOR,
    DeviceType.CONTACT_SENSOR -> false
}

/**
 * Whether this individual device is currently in its "on" / "active" state.
 */
fun Device.isActive(): Boolean = when (this) {
    is Light -> isOn
    is Lock -> isLocked
    is Switch -> isOn
    is SmartTv -> isOn
    is Thermostat -> isHeatingOn
    is ContactSensor -> isOpen
    is Blind -> openingLevel > 0
    is SmokeSensor -> isSmokeDetected
    is WaterLeakSensor -> isLeakDetected
    is TemperatureSensor -> true
}

/**
 * Returns a human-readable subtitle for the device state.
 */
fun Device.stateLabel(): String = when (this) {
    is Light -> if (isOn) "ON - ${brightness}%" else "OFF"
    is Lock -> if (isLocked) "Locked" else "Unlocked"
    is Switch -> if (isOn) "ON" else "OFF"
    is SmartTv -> if (isOn) "ON" else "OFF"
    is Thermostat -> if (isHeatingOn) "${currentTemperature}C" else "OFF"
    is ContactSensor -> if (isOpen) "Open" else "Closed"
    is Blind -> "${openingLevel}%"
    is SmokeSensor -> if (isSmokeDetected) "ALERT" else "OK"
    is WaterLeakSensor -> if (isLeakDetected) "ALERT" else "OK"
    is TemperatureSensor -> "${currentTemperature}C"
}

/**
 * Whether all devices in a list are in their active state.
 */
fun List<Device>.allActive(): Boolean = all { it.isActive() }
