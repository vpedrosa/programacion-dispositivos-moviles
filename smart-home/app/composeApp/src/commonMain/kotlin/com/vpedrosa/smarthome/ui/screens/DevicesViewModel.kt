package com.vpedrosa.smarthome.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.DeviceType
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
