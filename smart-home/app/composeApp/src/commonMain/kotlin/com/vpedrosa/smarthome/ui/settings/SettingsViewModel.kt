package com.vpedrosa.smarthome.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.settings.domain.model.AppSettings
import com.vpedrosa.smarthome.settings.domain.AppSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
)

class SettingsViewModel(
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = appSettingsRepository.observeSettings()
        .map { settings ->
            SettingsUiState(
                notificationsEnabled = settings.notificationsEnabled,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun toggleNotifications() {
        val current = uiState.value
        viewModelScope.launch {
            appSettingsRepository.saveSettings(
                AppSettings(notificationsEnabled = !current.notificationsEnabled),
            )
        }
    }
}
