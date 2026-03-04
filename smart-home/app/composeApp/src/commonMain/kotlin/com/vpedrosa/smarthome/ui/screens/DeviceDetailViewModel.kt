package com.vpedrosa.smarthome.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.Blind
import com.vpedrosa.smarthome.device.domain.Color as DomainColor
import com.vpedrosa.smarthome.device.domain.ContactSensor
import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceEvent
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.Lock
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.SmartTv
import com.vpedrosa.smarthome.device.domain.SmokeSensor
import com.vpedrosa.smarthome.device.domain.Switch
import com.vpedrosa.smarthome.device.domain.TemperatureSensor
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.WaterLeakSensor
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDeviceEventsUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveDeviceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ObserveRoomUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ToggleCastingUseCase
import com.vpedrosa.smarthome.device.domain.usecases.ToggleDeviceUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateBlindUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateLightUseCase
import com.vpedrosa.smarthome.device.domain.usecases.UpdateThermostatUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceDetailUiState(
    val device: Device? = null,
    val roomName: String? = null,
    val deviceEvents: List<DeviceEvent> = emptyList(),
    val isLoading: Boolean = true,
)

class DeviceDetailViewModel(
    private val deviceIdValue: String,
    private val observeDevice: ObserveDeviceUseCase,
    private val observeRoom: ObserveRoomUseCase,
    private val observeDeviceEvents: ObserveDeviceEventsUseCase,
    private val toggleDevice: ToggleDeviceUseCase,
    private val updateLight: UpdateLightUseCase,
    private val updateBlind: UpdateBlindUseCase,
    private val updateThermostat: UpdateThermostatUseCase,
    private val toggleCasting: ToggleCastingUseCase,
) : ViewModel() {

    private val deviceId = DeviceId(deviceIdValue)

    private val _uiState = MutableStateFlow(DeviceDetailUiState())
    val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

    init {
        observeDeviceAndRoom()
        observeEvents()
    }

    private fun observeDeviceAndRoom() {
        viewModelScope.launch {
            observeDevice(deviceId).collect { device ->
                if (device != null && device.roomId != null) {
                    observeRoom(RoomId(device.roomId!!)).collect { room ->
                        _uiState.update {
                            it.copy(
                                device = device,
                                roomName = room?.name,
                                isLoading = false,
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            device = device,
                            roomName = null,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            observeDeviceEvents()
                .map { events -> events.filter { it.deviceId == deviceId } }
                .collect { filteredEvents ->
                    _uiState.update { it.copy(deviceEvents = filteredEvents) }
                }
        }
    }

    fun onToggle() {
        viewModelScope.launch { toggleDevice(deviceId) }
    }

    fun onUpdateColor(color: DomainColor) {
        viewModelScope.launch { updateLight(deviceId, color = color) }
    }

    fun onUpdateBrightness(brightness: Int) {
        viewModelScope.launch { updateLight(deviceId, brightness = brightness) }
    }

    fun onUpdateOpeningLevel(level: Int) {
        viewModelScope.launch { updateBlind(deviceId, level) }
    }

    fun onUpdateTargetTemperature(temperature: Double) {
        viewModelScope.launch { updateThermostat(deviceId, targetTemperature = temperature) }
    }

    fun onToggleHeating() {
        viewModelScope.launch {
            val device = _uiState.value.device
            if (device is Thermostat) {
                updateThermostat(deviceId, isHeatingOn = !device.isHeatingOn)
            }
        }
    }

    fun onToggleCasting() {
        viewModelScope.launch { toggleCasting(deviceId) }
    }
}
