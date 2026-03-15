package com.vpedrosa.smarthome.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.device.domain.AppSettings
import com.vpedrosa.smarthome.device.domain.ports.AppSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val sensorAlertsEnabled: Boolean = true,
    val doorAlertEnabled: Boolean = true,
    val thermostatEventsEnabled: Boolean = false,
)

class SettingsViewModel(
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = appSettingsRepository.observeSettings()
        .map { settings ->
            SettingsUiState(
                sensorAlertsEnabled = settings.sensorAlertsEnabled,
                doorAlertEnabled = settings.doorAlertEnabled,
                thermostatEventsEnabled = settings.thermostatEventsEnabled,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun toggleSensorAlerts() {
        val current = uiState.value
        saveCurrentSettings(
            AppSettings(
                sensorAlertsEnabled = !current.sensorAlertsEnabled,
                doorAlertEnabled = current.doorAlertEnabled,
                thermostatEventsEnabled = current.thermostatEventsEnabled,
            )
        )
    }

    fun toggleDoorAlert() {
        val current = uiState.value
        saveCurrentSettings(
            AppSettings(
                sensorAlertsEnabled = current.sensorAlertsEnabled,
                doorAlertEnabled = !current.doorAlertEnabled,
                thermostatEventsEnabled = current.thermostatEventsEnabled,
            )
        )
    }

    fun toggleThermostatEvents() {
        val current = uiState.value
        saveCurrentSettings(
            AppSettings(
                sensorAlertsEnabled = current.sensorAlertsEnabled,
                doorAlertEnabled = current.doorAlertEnabled,
                thermostatEventsEnabled = !current.thermostatEventsEnabled,
            )
        )
    }

    private fun saveCurrentSettings(settings: AppSettings) {
        viewModelScope.launch {
            appSettingsRepository.saveSettings(settings)
        }
    }
}
