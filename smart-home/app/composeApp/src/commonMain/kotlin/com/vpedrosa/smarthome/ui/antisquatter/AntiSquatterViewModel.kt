package com.vpedrosa.smarthome.ui.antisquatter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.antisquatter.application.ToggleAntiSquatterUseCase
import com.vpedrosa.smarthome.antisquatter.application.UpdateAntiSquatterActionDurationUseCase
import com.vpedrosa.smarthome.antisquatter.application.UpdateAntiSquatterEndTimeUseCase
import com.vpedrosa.smarthome.antisquatter.application.UpdateAntiSquatterStartTimeUseCase
import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AntiSquatterUiState(
    val isEnabled: Boolean = false,
    val startHour: Int = 21,
    val startMinute: Int = 0,
    val endHour: Int = 23,
    val endMinute: Int = 0,
    val actionDurationMinutes: Int = 30,
    val isValid: Boolean = true,
    val maxActions: Int = 4,
)

class AntiSquatterViewModel(
    private val antiSquatterRepository: AntiSquatterRepository,
    private val toggleAntiSquatter: ToggleAntiSquatterUseCase,
    private val updateStartTime: UpdateAntiSquatterStartTimeUseCase,
    private val updateEndTime: UpdateAntiSquatterEndTimeUseCase,
    private val updateActionDuration: UpdateAntiSquatterActionDurationUseCase,
) : ViewModel() {

    val uiState: StateFlow<AntiSquatterUiState> = antiSquatterRepository.observeConfig()
        .map { config ->
            AntiSquatterUiState(
                isEnabled = config.isEnabled,
                startHour = config.startHour,
                startMinute = config.startMinute,
                endHour = config.endHour,
                endMinute = config.endMinute,
                actionDurationMinutes = config.actionDurationMinutes,
                isValid = config.isValid,
                maxActions = config.maxActions,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AntiSquatterUiState(),
        )

    fun toggleEnabled() {
        viewModelScope.launch { toggleAntiSquatter() }
    }

    fun updateStartTime(hour: Int, minute: Int) {
        viewModelScope.launch { updateStartTime(hour, minute) }
    }

    fun updateEndTime(hour: Int, minute: Int) {
        viewModelScope.launch { updateEndTime(hour, minute) }
    }

    fun updateActionDuration(minutes: Int) {
        viewModelScope.launch { updateActionDuration(minutes) }
    }
}
