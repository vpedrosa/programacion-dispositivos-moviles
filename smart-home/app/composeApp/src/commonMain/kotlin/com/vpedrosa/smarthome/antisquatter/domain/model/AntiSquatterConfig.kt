package com.vpedrosa.smarthome.antisquatter.domain.model

import com.vpedrosa.smarthome.shared.domain.model.RoomId
import kotlin.random.Random

data class LightTimeSlot(
    val id: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val roomIds: List<RoomId>,
) {
    init {
        require(startHour in 0..23) { "startHour must be in 0..23" }
        require(startMinute in 0..59) { "startMinute must be in 0..59" }
        require(endHour in 0..23) { "endHour must be in 0..23" }
        require(endMinute in 0..59) { "endMinute must be in 0..59" }
    }

    fun startTotalMinutes(): Int = startHour * 60 + startMinute

    fun endTotalMinutes(): Int = endHour * 60 + endMinute

    fun containsTime(hour: Int, minute: Int): Boolean {
        val time = hour * 60 + minute
        return time in startTotalMinutes() until endTotalMinutes()
    }

    fun formatTimeRange(): String {
        val start = "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}"
        val end = "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}"
        return "$start - $end"
    }

    companion object {
        fun createRandom(): LightTimeSlot {
            val startH = Random.nextInt(19, 21)
            val startM = listOf(0, 15, 30, 45).random()
            val durationMinutes = Random.nextInt(90, 181)
            val endTotal = startH * 60 + startM + durationMinutes
            val endH = (endTotal / 60).coerceAtMost(23)
            val endM = if (endTotal / 60 > 23) 30 else endTotal % 60
            return LightTimeSlot(
                id = "slot-${Random.nextInt(100_000, 999_999)}",
                startHour = startH,
                startMinute = startM,
                endHour = endH,
                endMinute = endM,
                roomIds = emptyList(),
            )
        }
    }
}

data class VideoConfig(
    val isEnabled: Boolean,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val videoUrl: String,
) {
    init {
        require(startHour in 0..23) { "startHour must be in 0..23" }
        require(startMinute in 0..59) { "startMinute must be in 0..59" }
        require(endHour in 0..23) { "endHour must be in 0..23" }
        require(endMinute in 0..59) { "endMinute must be in 0..59" }
    }

    fun containsTime(hour: Int, minute: Int): Boolean {
        val time = hour * 60 + minute
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute
        return time in start until end
    }

    fun formatTimeRange(): String {
        val start = "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}"
        val end = "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}"
        return "$start - $end"
    }
}

data class AntiSquatterConfig(
    val isEnabled: Boolean,
    val timeSlots: List<LightTimeSlot>,
    val videoConfig: VideoConfig,
) {
    companion object {
        val DEFAULT = AntiSquatterConfig(
            isEnabled = false,
            timeSlots = emptyList(),
            videoConfig = VideoConfig(
                isEnabled = false,
                startHour = 20,
                startMinute = 0,
                endHour = 22,
                endMinute = 0,
                videoUrl = "",
            ),
        )
    }
}
