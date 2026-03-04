package com.vpedrosa.smarthome.device.adapters.persistence

import com.vpedrosa.smarthome.device.domain.AntiSquatterConfig
import com.vpedrosa.smarthome.device.domain.LightTimeSlot
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.VideoConfig
import com.vpedrosa.smarthome.device.domain.ports.AntiSquatterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAntiSquatterRepository : AntiSquatterRepository {

    private val store = MutableStateFlow(defaultConfig())

    override fun observeConfig(): Flow<AntiSquatterConfig> = store.asStateFlow()

    override suspend fun saveConfig(config: AntiSquatterConfig) {
        store.value = config
    }

    companion object {
        fun defaultConfig(): AntiSquatterConfig = AntiSquatterConfig(
            isEnabled = false,
            timeSlots = listOf(
                LightTimeSlot(
                    id = "slot-1",
                    startHour = 18,
                    startMinute = 0,
                    endHour = 20,
                    endMinute = 0,
                    roomIds = listOf(RoomId("room-salon"), RoomId("room-cocina")),
                ),
                LightTimeSlot(
                    id = "slot-2",
                    startHour = 20,
                    startMinute = 0,
                    endHour = 22,
                    endMinute = 30,
                    roomIds = listOf(RoomId("room-salon"), RoomId("room-dormitorio")),
                ),
                LightTimeSlot(
                    id = "slot-3",
                    startHour = 22,
                    startMinute = 30,
                    endHour = 23,
                    endMinute = 30,
                    roomIds = listOf(RoomId("room-dormitorio")),
                ),
            ),
            videoConfig = VideoConfig(
                isEnabled = false,
                startHour = 20,
                startMinute = 0,
                endHour = 22,
                endMinute = 0,
                videoUrl = "https://youtube.com/watch?v=dQw4w9WgXcQ",
            ),
        )
    }
}
