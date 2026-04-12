package com.vpedrosa.smarthome.event.infrastructure

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vpedrosa.smarthome.device.domain.model.DeviceEvent
import com.vpedrosa.smarthome.device.domain.model.DeviceEventType
import com.vpedrosa.smarthome.event.domain.NotificationPort

class AndroidNotificationAdapter(
    private val context: Context,
) : NotificationPort {

    companion object {
        private const val CHANNEL_ID = NotificationChannels.SENSOR_ALERTS_ID
        private const val CHANNEL_NAME = NotificationChannels.SENSOR_ALERTS_NAME
        private const val CHANNEL_DESCRIPTION = NotificationChannels.SENSOR_ALERTS_DESCRIPTION
    }

    private val criticalTypes = setOf(
        DeviceEventType.SMOKE_ALERT,
        DeviceEventType.WATER_LEAK_ALERT,
        DeviceEventType.DOOR_OPEN_TOO_LONG,
    )

    init {
        createNotificationChannel()
    }

    override fun showSensorAlert(event: DeviceEvent) {
        val (title, icon) = titleAndIconFor(event.type)
        val priority = if (event.type in criticalTypes) {
            NotificationCompat.PRIORITY_HIGH
        } else {
            NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(event.message)
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(event.id.hashCode(), notification)
    }

    private fun titleAndIconFor(type: DeviceEventType): Pair<String, Int> = when (type) {
        DeviceEventType.SMOKE_ALERT -> "Smoke Alert" to android.R.drawable.ic_dialog_alert
        DeviceEventType.WATER_LEAK_ALERT -> "Water Leak Alert" to android.R.drawable.ic_dialog_alert
        DeviceEventType.DOOR_OPEN_TOO_LONG -> "Door Open Alert" to android.R.drawable.ic_lock_idle_alarm
        DeviceEventType.DOOR_OPENED -> "Door Opened" to android.R.drawable.ic_lock_idle_lock
        DeviceEventType.DOOR_CLOSED -> "Door Closed" to android.R.drawable.ic_lock_lock
        DeviceEventType.TEMPERATURE_READING -> "Temperature" to android.R.drawable.ic_dialog_info
        DeviceEventType.THERMOSTAT_ADJUSTED -> "Thermostat" to android.R.drawable.ic_dialog_info
        DeviceEventType.DEVICE_TURNED_ON -> "Device On" to android.R.drawable.ic_dialog_info
        DeviceEventType.DEVICE_TURNED_OFF -> "Device Off" to android.R.drawable.ic_dialog_info
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
