package com.vpedrosa.smarthome.antisquatter.domain.model

data class AntiSquatterConfig(
    val isEnabled: Boolean,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val actionDurationMinutes: Int,
) {
    init {
        require(startHour in 0..23)
        require(startMinute in 0..59)
        require(endHour in 0..23)
        require(endMinute in 0..59)
        require(actionDurationMinutes > 0)
    }

    val totalIntervalMinutes: Int
        get() = endHour * 60 + endMinute - (startHour * 60 + startMinute)

    val maxActions: Int
        get() = if (actionDurationMinutes > 0) totalIntervalMinutes / actionDurationMinutes else 0

    val isValid: Boolean
        get() = totalIntervalMinutes > 0 && actionDurationMinutes <= totalIntervalMinutes

    fun formatStartTime(): String =
        "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}"

    fun formatEndTime(): String =
        "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}"

    companion object {
        val DEFAULT = AntiSquatterConfig(
            isEnabled = false,
            startHour = 21,
            startMinute = 0,
            endHour = 23,
            endMinute = 0,
            actionDurationMinutes = 30,
        )
    }
}
