package com.vpedrosa.smarthome.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.model.Blind
import com.vpedrosa.smarthome.device.domain.model.Color as DomainColor
import com.vpedrosa.smarthome.device.domain.model.ContactSensor
import com.vpedrosa.smarthome.device.domain.model.Device
import com.vpedrosa.smarthome.device.domain.model.DeviceEvent
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.Light
import com.vpedrosa.smarthome.device.domain.model.Lock
import com.vpedrosa.smarthome.device.domain.model.SmokeSensor
import com.vpedrosa.smarthome.device.domain.model.Switch
import com.vpedrosa.smarthome.device.domain.model.TemperatureSensor
import com.vpedrosa.smarthome.device.domain.model.Thermostat
import com.vpedrosa.smarthome.device.domain.model.WaterLeakSensor
import com.vpedrosa.smarthome.device.domain.DeviceEventRepository
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import com.vpedrosa.smarthome.room.domain.RoomRepository
import com.vpedrosa.smarthome.device.application.DeregisterDeviceUseCase
import com.vpedrosa.smarthome.device.application.LaunchContentUseCase
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import com.vpedrosa.smarthome.device.application.UpdateBlindUseCase
import com.vpedrosa.smarthome.device.application.UpdateLightUseCase
import com.vpedrosa.smarthome.device.application.UpdateThermostatUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val isActionInProgress: Boolean = false,
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
    private val launchContent: LaunchContentUseCase,
    private val deregisterDevice: DeregisterDeviceUseCase,
) : ViewModel() {

    private val deviceId = DeviceId(deviceIdValue)
    private var controlJob: Job? = null

    private val _uiState = MutableStateFlow(DeviceDetailUiState())
    val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

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

    fun onToggle() = withActionLoading { toggleDevice(deviceId) }

    fun onUpdateColor(color: DomainColor) = withActionLoading { updateLight(deviceId, color = color) }

    fun onUpdateBrightness(brightness: Int) = withActionLoading { updateLight(deviceId, brightness = brightness) }

    fun onUpdateOpeningLevel(level: Int) = withActionLoading { updateBlind(deviceId, level) }

    fun onUpdateTargetTemperature(temperature: Double) {
        controlJob?.cancel()
        controlJob = viewModelScope.launch {
            delay(CONTROL_DEBOUNCE_MS)
            _uiState.update { it.copy(isActionInProgress = true) }
            runCatching { updateThermostat(deviceId, targetTemperature = temperature) }
            _uiState.update { it.copy(isActionInProgress = false) }
        }
    }

    fun onToggleHeating() {
        withActionLoading {
            val device = deviceRepository.observeDevice(deviceId).first()
            if (device is Thermostat) {
                updateThermostat(deviceId, isHeatingOn = !device.isHeatingOn)
            }
        }
    }

    fun onLaunchContent(url: String) = withActionLoading { launchContent(deviceId, url) }

    fun onDeregister() {
        viewModelScope.launch {
            deregisterDevice(deviceId)
            _navigateBack.emit(Unit)
        }
    }

    private fun withActionLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            runCatching { block() }
            _uiState.update { it.copy(isActionInProgress = false) }
        }
    }

    private companion object {
        const val CONTROL_DEBOUNCE_MS = 300L
    }
}
