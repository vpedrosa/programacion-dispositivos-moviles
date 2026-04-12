package com.vpedrosa.smarthome.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.commissioning.application.ClearSimulatorHostUseCase
import com.vpedrosa.smarthome.commissioning.application.SearchSimulatorUseCase
import com.vpedrosa.smarthome.commissioning.domain.SimulatorHostRepository
import com.vpedrosa.smarthome.settings.application.ToggleNotificationsUseCase
import com.vpedrosa.smarthome.settings.domain.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val simulatorHost: String? = null,
    val isSearching: Boolean = false,
    val searchError: Boolean = false,
)

class SettingsViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val simulatorHostRepository: SimulatorHostRepository,
    private val toggleNotificationsUseCase: ToggleNotificationsUseCase,
    private val searchSimulatorUseCase: SearchSimulatorUseCase,
    private val clearSimulatorHostUseCase: ClearSimulatorHostUseCase,
) : ViewModel() {

    private val searchState = MutableStateFlow(SearchState())

    val uiState: StateFlow<SettingsUiState> = combine(
        appSettingsRepository.observeSettings(),
        simulatorHostRepository.observeHost(),
        searchState,
    ) { settings, host, search ->
        SettingsUiState(
            notificationsEnabled = settings.notificationsEnabled,
            simulatorHost = host,
            isSearching = search.isSearching,
            searchError = search.searchError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun toggleNotifications() {
        viewModelScope.launch {
            toggleNotificationsUseCase()
        }
    }

    fun searchSimulator() {
        viewModelScope.launch {
            searchState.update { it.copy(isSearching = true, searchError = false) }
            val found = searchSimulatorUseCase()
            searchState.update { it.copy(isSearching = false, searchError = !found) }
        }
    }

    fun clearSimulatorHost() {
        viewModelScope.launch {
            clearSimulatorHostUseCase()
        }
    }

    private data class SearchState(
        val isSearching: Boolean = false,
        val searchError: Boolean = false,
    )
}
