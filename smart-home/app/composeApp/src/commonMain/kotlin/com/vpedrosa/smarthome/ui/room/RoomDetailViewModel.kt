package com.vpedrosa.smarthome.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.model.Device
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.DeviceType
import com.vpedrosa.smarthome.device.domain.model.RoomId
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import com.vpedrosa.smarthome.room.domain.RoomRepository
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeInRoomUseCase
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoomDetailUiState(
    val roomName: String = "",
    val devicesByType: Map<DeviceType, List<Device>> = emptyMap(),
    val togglingDevices: Set<DeviceId> = emptySet(),
    val bulkTogglingTypes: Set<DeviceType> = emptySet(),
)

class RoomDetailViewModel(
    private val roomIdValue: String,
    deviceRepository: DeviceRepository,
    roomRepository: RoomRepository,
    private val toggleDevice: ToggleDeviceUseCase,
    private val bulkToggleByTypeInRoom: BulkToggleDevicesByTypeInRoomUseCase,
) : ViewModel() {

    private val roomId = RoomId(roomIdValue)
    private val _actionState = MutableStateFlow(ActionState())

    val uiState: StateFlow<RoomDetailUiState> = combine(
        deviceRepository.observeAllDevices(),
        roomRepository.observeRoom(roomId),
        _actionState,
    ) { allDevices, room, action ->
        val deviceMap = allDevices.associateBy { it.id }
        val roomDevices = room?.deviceIds?.mapNotNull { deviceMap[it] } ?: emptyList()

        val grouped = roomDevices
            .groupBy { it.type }
            .toSortedMap(compareBy { it.ordinal })

        RoomDetailUiState(
            roomName = room?.name ?: "",
            devicesByType = grouped,
            togglingDevices = action.togglingDevices,
            bulkTogglingTypes = action.bulkTogglingTypes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoomDetailUiState(),
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
            runCatching { bulkToggleByTypeInRoom(roomId, type, turnOn) }
            _actionState.update { it.copy(bulkTogglingTypes = it.bulkTogglingTypes - type) }
        }
    }

    private data class ActionState(
        val togglingDevices: Set<DeviceId> = emptySet(),
        val bulkTogglingTypes: Set<DeviceType> = emptySet(),
    )
}
