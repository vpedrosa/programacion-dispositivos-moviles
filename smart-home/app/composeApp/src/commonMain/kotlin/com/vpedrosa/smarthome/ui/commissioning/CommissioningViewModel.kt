package com.vpedrosa.smarthome.ui.commissioning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.commissioning.domain.DeviceDiscoveryPort
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import com.vpedrosa.smarthome.commissioning.application.CommissionDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommissioningUiState(
    val allDevices: List<DiscoveredDevice> = emptyList(),
    val commissionedSerials: Set<String> = emptySet(),
    val commissioningInProgress: Set<String> = emptySet(),
    val lastError: String? = null,
    val successMessage: String? = null,
)

class CommissioningViewModel(
    discoveryPort: DeviceDiscoveryPort,
    deviceRepository: DeviceRepository,
    private val commissionDevice: CommissionDeviceUseCase,
) : ViewModel() {

    private val inProgress = MutableStateFlow<Set<String>>(emptySet())
    private val error = MutableStateFlow<String?>(null)
    private val success = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CommissioningUiState> = combine(
        discoveryPort.discoverDevices(),
        deviceRepository.observeAllDevices(),
        inProgress,
        error,
        success,
    ) { discovered, commissioned, progressing, lastError, successMsg ->
        val commissionedIds = commissioned.map { it.id.value }.toSet()
        CommissioningUiState(
            allDevices = discovered,
            commissionedSerials = commissionedIds,
            commissioningInProgress = progressing,
            lastError = lastError,
            successMessage = successMsg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CommissioningUiState())

    fun commission(device: DiscoveredDevice, customName: String? = null) {
        viewModelScope.launch {
            inProgress.update { it + device.serialNumber }
            error.value = null
            success.value = null

            val displayName = customName?.takeIf { it.isNotBlank() } ?: device.name
            commissionDevice(device, customName)
                .onSuccess { success.value = displayName }
                .onFailure { error.value = "$displayName: ${it.message}" }

            inProgress.update { it - device.serialNumber }
        }
    }

    fun dismissError() {
        error.value = null
    }

    fun dismissSuccess() {
        success.value = null
    }
}
