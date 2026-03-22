package com.vpedrosa.smarthome.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.shared.domain.model.DeviceEvent
import com.vpedrosa.smarthome.shared.domain.model.DeviceEventType
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.shared.domain.model.isActive
import com.vpedrosa.smarthome.shared.domain.model.Lock
import com.vpedrosa.smarthome.shared.domain.model.Room
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.shared.domain.model.TemperatureSensor
import com.vpedrosa.smarthome.shared.domain.DeviceEventRepository
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.shared.domain.EnvironmentPort
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val lightsOnCount: Int = 0,
    val locksCount: Int = 0,
    val temperature: String = "--",
    val hasSmartTv: Boolean = false,
    val isSmartTvOn: Boolean = false,
    val recentAlerts: List<AlertItem> = emptyList(),
    val rooms: List<RoomSummary> = emptyList(),
    val showCommissioningButton: Boolean = false,
)

data class AlertItem(
    val id: String,
    val message: String,
    val type: DeviceEventType,
)

data class RoomSummary(
    val id: String,
    val name: String,
    val activeDeviceCount: Int,
    val photoUri: String?,
)

class DashboardViewModel(
    deviceRepository: DeviceRepository,
    roomRepository: RoomRepository,
    deviceEventRepository: DeviceEventRepository,
    environmentPort: EnvironmentPort,
) : ViewModel() {

    val showCommissioningButton: Boolean = environmentPort.isEmulator

    val uiState: StateFlow<DashboardUiState> = combine(
        deviceRepository.observeAllDevices(),
        roomRepository.observeAllRooms(),
        deviceEventRepository.observeAllEvents(),
    ) { devices, rooms, events ->

        val lightsOn = devices.count { it is Light && it.isOn }
        val locksCount = devices.count { it is Lock }
        val tempSensor = devices.filterIsInstance<TemperatureSensor>().firstOrNull()
        val temperatureText = tempSensor?.let { "${it.currentTemperature}\u00B0" } ?: "--"
        val smartTv = devices.filterIsInstance<SmartTv>().firstOrNull()
        val tvOn = smartTv?.isOn == true

        val recentAlerts = events
            .sortedByDescending { it.timestamp }
            .take(5)
            .map { it.toAlertItem() }

        val roomSummaries = rooms.map { room ->
            val activeCount = room.deviceIds.count { deviceId ->
                devices.find { it.id == deviceId }?.let { device ->
                    device.isActive()
                } ?: false
            }
            RoomSummary(
                id = room.id.value,
                name = room.name,
                activeDeviceCount = activeCount,
                photoUri = room.photoUri,
            )
        }

        DashboardUiState(
            lightsOnCount = lightsOn,
            locksCount = locksCount,
            temperature = temperatureText,
            hasSmartTv = smartTv != null,
            isSmartTvOn = tvOn,
            recentAlerts = recentAlerts,
            rooms = roomSummaries,
            showCommissioningButton = showCommissioningButton,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )
}

private fun DeviceEvent.toAlertItem() = AlertItem(
    id = id,
    message = message,
    type = type,
)
