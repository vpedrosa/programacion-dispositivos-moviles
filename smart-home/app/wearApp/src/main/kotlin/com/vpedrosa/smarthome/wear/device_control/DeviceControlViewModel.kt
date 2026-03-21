package com.vpedrosa.smarthome.wear.device_control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.wear.device_control.domain.ports.ActionResult
import com.vpedrosa.smarthome.wear.device_control.domain.ports.DeviceCommandPort
import com.vpedrosa.smarthome.wear.device_control.domain.ports.DeviceListResult
import com.vpedrosa.smarthome.wear.device_control.model.WearDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceControlViewModel(
    private val deviceCommandPort: DeviceCommandPort,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceControlUiState())
    val uiState: StateFlow<DeviceControlUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = deviceCommandPort.requestDeviceList()) {
                is DeviceListResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        devicesByRoom = groupByRoom(result.devices),
                    )
                }
                is DeviceListResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    fun toggleDevice(deviceId: String) {
        viewModelScope.launch {
            when (val result = deviceCommandPort.sendToggleAction(deviceId)) {
                is ActionResult.Success -> {
                    val updated = result.updatedDevice
                    val currentMap = _uiState.value.devicesByRoom.toMutableMap()
                    for ((room, devices) in currentMap) {
                        val index = devices.indexOfFirst { it.id == updated.id }
                        if (index != -1) {
                            currentMap[room] = devices.toMutableList().apply { set(index, updated) }
                            break
                        }
                    }
                    _uiState.value = _uiState.value.copy(devicesByRoom = currentMap)
                }
                is ActionResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }

    private fun groupByRoom(devices: List<WearDevice>): Map<String, List<WearDevice>> {
        val grouped = linkedMapOf<String, MutableList<WearDevice>>()
        for (device in devices) {
            val room = device.roomName ?: UNASSIGNED_ROOM
            grouped.getOrPut(room) { mutableListOf() }.add(device)
        }
        return grouped
    }

    companion object {
        const val UNASSIGNED_ROOM = "__unassigned__"
    }
}

data class DeviceControlUiState(
    val isLoading: Boolean = false,
    val devicesByRoom: Map<String, List<WearDevice>> = emptyMap(),
    val error: String? = null,
)
