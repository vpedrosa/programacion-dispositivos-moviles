package com.vpedrosa.smarthome.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.DeviceType
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.usecases.BulkToggleDevicesByTypeInRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllDevicesUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ToggleDeviceUseCase
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
    observeAllDevices: ObserveAllDevicesUseCase,
    observeRoom: ObserveRoomUseCase,
    private val toggleDevice: ToggleDeviceUseCase,
    private val bulkToggleByTypeInRoom: BulkToggleDevicesByTypeInRoomUseCase,
) : ViewModel() {

    private val roomId = RoomId(roomIdValue)

    val uiState: StateFlow<RoomDetailUiState> = combine(
        observeAllDevices(),
        observeRoom(roomId),
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
            toggleDevice(deviceId)
        }
    }

    fun onBulkToggle(type: DeviceType, turnOn: Boolean) {
        viewModelScope.launch {
            bulkToggleByTypeInRoom(roomId, type, turnOn)
        }
    }
}
