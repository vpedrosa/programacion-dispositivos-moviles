package com.vpedrosa.smarthome.room.application

import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.Room
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.shared.domain.RoomRepository
import kotlin.random.Random

class SaveRoomUseCase(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(
        existingRoomId: RoomId?,
        name: String,
        photoUri: String?,
        deviceIds: List<DeviceId>,
    ) {
        val roomId = existingRoomId
            ?: RoomId("room-${name.lowercase().replace(" ", "-")}-${Random.nextInt(100_000, 999_999)}")

        val room = Room(
            id = roomId,
            name = name.trim(),
            photoUri = photoUri,
            deviceIds = deviceIds,
        )
        roomRepository.save(room)
    }
}
