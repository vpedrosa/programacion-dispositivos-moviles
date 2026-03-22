package com.vpedrosa.smarthome.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import com.vpedrosa.smarthome.room.application.SaveRoomUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditGroupUiState(
    val name: String = "",
    val photoUri: String? = null,
    val selectedDeviceIds: Set<DeviceId> = emptySet(),
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
)

class EditGroupViewModel(
    private val roomIdValue: String?,
    deviceRepository: DeviceRepository,
    private val roomRepository: RoomRepository,
    private val saveRoom: SaveRoomUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditGroupUiState())
    val uiState: StateFlow<EditGroupUiState> = _uiState.asStateFlow()

    val allDevices: StateFlow<List<Device>> = deviceRepository.observeAllDevices()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    init {
        if (roomIdValue != null) {
            loadRoom(RoomId(roomIdValue))
        }
    }

    private fun loadRoom(roomId: RoomId) {
        viewModelScope.launch {
            val room = roomRepository.observeRoom(roomId).first()
            if (room != null) {
                _uiState.update {
                    it.copy(
                        name = room.name,
                        photoUri = room.photoUri,
                        selectedDeviceIds = room.deviceIds.toSet(),
                        isEditing = true,
                    )
                }
            }
        }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun setPhotoUri(uri: String?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun toggleDevice(deviceId: DeviceId) {
        _uiState.update { state ->
            val updated = if (deviceId in state.selectedDeviceIds) {
                state.selectedDeviceIds - deviceId
            } else {
                state.selectedDeviceIds + deviceId
            }
            state.copy(selectedDeviceIds = updated)
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            saveRoom(
                existingRoomId = roomIdValue?.let { RoomId(it) },
                name = state.name,
                photoUri = state.photoUri,
                deviceIds = state.selectedDeviceIds.toList(),
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
