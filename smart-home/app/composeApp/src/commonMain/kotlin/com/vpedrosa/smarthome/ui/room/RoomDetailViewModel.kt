package com.vpedrosa.smarthome.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeInRoomUseCase
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoomDetailUiState(
    val roomName: String = "",
    val devicesByType: Map<DeviceType, List<Device>> = emptyMap(),
)

class RoomDetailViewModel(
    private val roomIdValue: String,
    deviceRepository: DeviceRepository,
    roomRepository: RoomRepository,
    private val toggleDevice: ToggleDeviceUseCase,
    private val bulkToggleByTypeInRoom: BulkToggleDevicesByTypeInRoomUseCase,
) : ViewModel() {

    private val roomId = RoomId(roomIdValue)

    val uiState: StateFlow<RoomDetailUiState> = combine(
        deviceRepository.observeAllDevices(),
        roomRepository.observeRoom(roomId),
    ) { allDevices, room ->
        val deviceMap = allDevices.associateBy { it.id }
        val roomDevices = room?.deviceIds?.mapNotNull { deviceMap[it] } ?: emptyList()

        val grouped = roomDevices
            .groupBy { it.type }
            .toSortedMap(compareBy { it.ordinal })

        RoomDetailUiState(
            roomName = room?.name ?: "",
            devicesByType = grouped,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoomDetailUiState(),
    )

    fun onToggleDevice(deviceId: DeviceId) {
        viewModelScope.launch {
            runCatching { toggleDevice(deviceId) }
        }
    }

    fun onBulkToggle(type: DeviceType, turnOn: Boolean) {
        viewModelScope.launch {
            runCatching { bulkToggleByTypeInRoom(roomId, type, turnOn) }
        }
    }
}
