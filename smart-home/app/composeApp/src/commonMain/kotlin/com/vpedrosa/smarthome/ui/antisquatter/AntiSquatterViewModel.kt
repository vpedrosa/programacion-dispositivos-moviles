package com.vpedrosa.smarthome.ui.antisquatter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpedrosa.smarthome.antisquatter.domain.model.AntiSquatterConfig
import com.vpedrosa.smarthome.antisquatter.domain.model.LightTimeSlot
import com.vpedrosa.smarthome.shared.domain.model.Room
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.antisquatter.domain.model.VideoConfig
import com.vpedrosa.smarthome.antisquatter.domain.AntiSquatterRepository
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class AntiSquatterUiState(
    val isEnabled: Boolean = false,
    val timeSlots: List<LightTimeSlot> = emptyList(),
    val videoConfig: VideoConfig = VideoConfig(
        isEnabled = false,
        startHour = 20,
        startMinute = 0,
        endHour = 22,
        endMinute = 0,
        videoUrl = "",
    ),
    val rooms: List<Room> = emptyList(),
)

class AntiSquatterViewModel(
    private val antiSquatterRepository: AntiSquatterRepository,
    roomRepository: RoomRepository,
) : ViewModel() {

    val uiState: StateFlow<AntiSquatterUiState> = combine(
        antiSquatterRepository.observeConfig(),
        roomRepository.observeAllRooms(),
    ) { config, rooms ->
        AntiSquatterUiState(
            isEnabled = config.isEnabled,
            timeSlots = config.timeSlots,
            videoConfig = config.videoConfig,
            rooms = rooms,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AntiSquatterUiState(),
    )

    fun toggleEnabled() {
        val current = uiState.value
        saveCurrentConfig(
            AntiSquatterConfig(
                isEnabled = !current.isEnabled,
                timeSlots = current.timeSlots,
                videoConfig = current.videoConfig,
            )
        )
    }

    fun addTimeSlot() {
        val current = uiState.value
        val newSlot = LightTimeSlot(
            id = "slot-${Random.nextInt(100_000, 999_999)}",
            startHour = 18,
            startMinute = 0,
            endHour = 20,
            endMinute = 0,
            roomIds = emptyList(),
        )
        saveCurrentConfig(
            AntiSquatterConfig(
                isEnabled = current.isEnabled,
                timeSlots = current.timeSlots + newSlot,
                videoConfig = current.videoConfig,
            )
        )
    }

    fun removeTimeSlot(slotId: String) {
        val current = uiState.value
        saveCurrentConfig(
            AntiSquatterConfig(
                isEnabled = current.isEnabled,
                timeSlots = current.timeSlots.filter { it.id != slotId },
                videoConfig = current.videoConfig,
            )
        )
    }

    fun updateTimeSlot(updated: LightTimeSlot) {
        val current = uiState.value
        saveCurrentConfig(
            AntiSquatterConfig(
                isEnabled = current.isEnabled,
                timeSlots = current.timeSlots.map { if (it.id == updated.id) updated else it },
                videoConfig = current.videoConfig,
            )
        )
    }

    fun toggleVideoEnabled() {
        val current = uiState.value
        saveCurrentConfig(
            AntiSquatterConfig(
                isEnabled = current.isEnabled,
                timeSlots = current.timeSlots,
                videoConfig = current.videoConfig.copy(isEnabled = !current.videoConfig.isEnabled),
            )
        )
    }

    fun updateVideoUrl(url: String) {
        val current = uiState.value
        saveCurrentConfig(
            AntiSquatterConfig(
                isEnabled = current.isEnabled,
                timeSlots = current.timeSlots,
                videoConfig = current.videoConfig.copy(videoUrl = url),
            )
        )
    }

    fun updateVideoTime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val current = uiState.value
        saveCurrentConfig(
            AntiSquatterConfig(
                isEnabled = current.isEnabled,
                timeSlots = current.timeSlots,
                videoConfig = current.videoConfig.copy(
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                ),
            )
        )
    }

    private fun saveCurrentConfig(config: AntiSquatterConfig) {
        viewModelScope.launch {
            antiSquatterRepository.saveConfig(config)
        }
    }
}
