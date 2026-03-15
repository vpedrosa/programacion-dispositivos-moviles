package com.vpedrosa.smarthome.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.DiscoveredDevice
import com.vpedrosa.smarthome.device.domain.ports.DeviceDiscoveryPort
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import com.vpedrosa.smarthome.device.domain.usecases.CommissionDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommissioningUiState(
    val availableDevices: List<DiscoveredDevice> = emptyList(),
    val commissionedSerials: Set<String> = emptySet(),
    val commissioningInProgress: Set<String> = emptySet(),
    val lastError: String? = null,
)

class CommissioningViewModel(
    discoveryPort: DeviceDiscoveryPort,
    deviceRepository: DeviceRepository,
    private val commissionDevice: CommissionDeviceUseCase,
) : ViewModel() {

    private val inProgress = MutableStateFlow<Set<String>>(emptySet())
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CommissioningUiState> = combine(
        discoveryPort.discoverDevices(),
        deviceRepository.observeAllDevices(),
        inProgress,
        error,
    ) { discovered, commissioned, progressing, lastError ->
        val commissionedIds = commissioned.map { it.id.value }.toSet()
        CommissioningUiState(
            availableDevices = discovered.filterNot { it.serialNumber in commissionedIds },
            commissionedSerials = commissionedIds,
            commissioningInProgress = progressing,
            lastError = lastError,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CommissioningUiState())

    fun commission(device: DiscoveredDevice) {
        viewModelScope.launch {
            inProgress.update { it + device.serialNumber }
            error.value = null

            commissionDevice(device)
                .onFailure { error.value = "${device.name}: ${it.message}" }

            inProgress.update { it - device.serialNumber }
        }
    }

    fun dismissError() {
        error.value = null
    }
}
