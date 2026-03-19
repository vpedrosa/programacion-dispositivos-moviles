package com.vpedrosa.smarthome.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.Color as DomainColor
import com.vpedrosa.smarthome.shared.domain.model.ContactSensor
import com.vpedrosa.smarthome.shared.domain.model.Device
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.shared.domain.model.Lock
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.shared.domain.model.SmokeSensor
import com.vpedrosa.smarthome.shared.domain.model.Switch
import com.vpedrosa.smarthome.shared.domain.model.TemperatureSensor
import com.vpedrosa.smarthome.shared.domain.model.Thermostat
import com.vpedrosa.smarthome.shared.domain.model.WaterLeakSensor
import com.vpedrosa.smarthome.shared.domain.DeviceEventRepository
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import com.vpedrosa.smarthome.device.application.UpdateBlindUseCase
import com.vpedrosa.smarthome.device.application.UpdateLightUseCase
import com.vpedrosa.smarthome.device.application.UpdateThermostatUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val deviceRepository: DeviceRepository,
    private val roomRepository: RoomRepository,
    private val deviceEventRepository: DeviceEventRepository,
    private val toggleDevice: ToggleDeviceUseCase,
    private val updateLight: UpdateLightUseCase,
    private val updateBlind: UpdateBlindUseCase,
    private val updateThermostat: UpdateThermostatUseCase,
) : ViewModel() {

    private val deviceId = DeviceId(deviceIdValue)
    private var controlJob: Job? = null

    private val _uiState = MutableStateFlow(DeviceDetailUiState())
    val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

    init {
        observeDeviceAndRoom()
        observeEvents()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDeviceAndRoom() {
        viewModelScope.launch {
            deviceRepository.observeDevice(deviceId)
                .flatMapLatest { device ->
                    if (device?.roomId != null) {
                        roomRepository.observeRoom(device.roomId!!).map { room ->
                            device to room
                        }
                    } else {
                        flowOf(device to null)
                    }
                }
                .collect { (device, room) ->
                    _uiState.update {
                        it.copy(
                            device = device,
                            roomName = room?.name,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            deviceEventRepository.observeAllEvents()
                .map { events -> events.filter { it.deviceId == deviceId } }
                .collect { filteredEvents ->
                    _uiState.update { it.copy(deviceEvents = filteredEvents) }
                }
        }
    }

    fun onToggle() {
        viewModelScope.launch { runCatching { toggleDevice(deviceId) } }
    }

    fun onUpdateColor(color: DomainColor) {
        viewModelScope.launch { runCatching { updateLight(deviceId, color = color) } }
    }

    fun onUpdateBrightness(brightness: Int) {
        viewModelScope.launch { runCatching { updateLight(deviceId, brightness = brightness) } }
    }

    fun onUpdateOpeningLevel(level: Int) {
        viewModelScope.launch { runCatching { updateBlind(deviceId, level) } }
    }

    fun onUpdateTargetTemperature(temperature: Double) {
        debouncedControl { updateThermostat(deviceId, targetTemperature = temperature) }
    }

    fun onToggleHeating() {
        viewModelScope.launch {
            val device = _uiState.value.device
            if (device is Thermostat) {
                runCatching { updateThermostat(deviceId, isHeatingOn = !device.isHeatingOn) }
            }
        }
    }

    fun onToggleCasting() {
        viewModelScope.launch {
            val device = deviceRepository.observeDevice(deviceId).first() ?: return@launch
            if (device !is SmartTv) return@launch
            deviceRepository.save(device.toggleCasting())
        }
    }

    private fun debouncedControl(block: suspend () -> Unit) {
        controlJob?.cancel()
        controlJob = viewModelScope.launch {
            delay(CONTROL_DEBOUNCE_MS)
            runCatching { block() }
        }
    }

    private companion object {
        const val CONTROL_DEBOUNCE_MS = 300L
    }
}
