package com.vpedrosa.smarthome.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.isActive
import com.vpedrosa.smarthome.device.domain.Room
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.usecases.DeleteRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllDevicesUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveAllRoomsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoomCardInfo(
    val room: Room,
    val deviceCount: Int,
    val activeCount: Int,
)

data class RoomsUiState(
    val rooms: List<RoomCardInfo> = emptyList(),
)

class RoomsViewModel(
    observeAllRooms: ObserveAllRoomsUseCase,
    observeAllDevices: ObserveAllDevicesUseCase,
    private val deleteRoom: DeleteRoomUseCase,
) : ViewModel() {

    val uiState: StateFlow<RoomsUiState> = combine(
        observeAllRooms(),
        observeAllDevices(),
    ) { rooms, devices ->
        val deviceMap = devices.associateBy { it.id }

        val summaries = rooms.map { room ->
            val roomDevices = room.deviceIds.mapNotNull { deviceMap[it] }
            RoomCardInfo(
                room = room,
                deviceCount = roomDevices.size,
                activeCount = roomDevices.count { it.isActive() },
            )
        }

        RoomsUiState(rooms = summaries)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoomsUiState(),
    )

    fun onDeleteRoom(roomId: RoomId) {
        viewModelScope.launch {
            deleteRoom(roomId)
        }
    }
}
