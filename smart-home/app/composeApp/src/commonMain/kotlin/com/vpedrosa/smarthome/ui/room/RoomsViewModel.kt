package com.vpedrosa.smarthome.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.isActive
import com.vpedrosa.smarthome.shared.domain.model.Room
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
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
    private val roomRepository: RoomRepository,
    deviceRepository: DeviceRepository,
) : ViewModel() {

    val uiState: StateFlow<RoomsUiState> = combine(
        roomRepository.observeAllRooms(),
        deviceRepository.observeAllDevices(),
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
            roomRepository.delete(roomId)
        }
    }
}
