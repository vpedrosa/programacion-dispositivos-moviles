package com.vpedrosa.smarthome.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DevicesUiState(
    val devicesByType: Map<DeviceType, List<Device>> = emptyMap(),
    val roomNames: Map<RoomId, String> = emptyMap(),
    val togglingDevices: Set<DeviceId> = emptySet(),
    val bulkTogglingTypes: Set<DeviceType> = emptySet(),
)

class DevicesViewModel(
    deviceRepository: DeviceRepository,
    roomRepository: RoomRepository,
    private val toggleDevice: ToggleDeviceUseCase,
    private val bulkToggleDevicesByType: BulkToggleDevicesByTypeUseCase,
) : ViewModel() {

    private val _actionState = MutableStateFlow(ActionState())

    val uiState: StateFlow<DevicesUiState> = combine(
        deviceRepository.observeAllDevices(),
        roomRepository.observeAllRooms(),
        _actionState,
    ) { devices, rooms, action ->
        val grouped = devices
            .groupBy { it.type }
            .toSortedMap(compareBy { it.ordinal })

        val roomNamesMap = rooms.associate { it.id to it.name }

        DevicesUiState(
            devicesByType = grouped,
            roomNames = roomNamesMap,
            togglingDevices = action.togglingDevices,
            bulkTogglingTypes = action.bulkTogglingTypes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DevicesUiState(),
    )

    fun onToggleDevice(deviceId: DeviceId) {
        viewModelScope.launch {
            _actionState.update { it.copy(togglingDevices = it.togglingDevices + deviceId) }
            runCatching { toggleDevice(deviceId) }
            _actionState.update { it.copy(togglingDevices = it.togglingDevices - deviceId) }
        }
    }

    fun onBulkToggle(type: DeviceType, turnOn: Boolean) {
        viewModelScope.launch {
            _actionState.update { it.copy(bulkTogglingTypes = it.bulkTogglingTypes + type) }
            runCatching { bulkToggleDevicesByType(type, turnOn) }
            _actionState.update { it.copy(bulkTogglingTypes = it.bulkTogglingTypes - type) }
        }
    }

    private data class ActionState(
        val togglingDevices: Set<DeviceId> = emptySet(),
        val bulkTogglingTypes: Set<DeviceType> = emptySet(),
    )
}
