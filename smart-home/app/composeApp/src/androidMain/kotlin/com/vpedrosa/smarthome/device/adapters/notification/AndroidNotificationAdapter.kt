package com.vpedrosa.smarthome.device.adapters.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vpedrosa.smarthome.device.domain.DeviceEvent
import com.vpedrosa.smarthome.device.domain.DeviceEventType
import com.vpedrosa.smarthome.device.domain.ports.NotificationPort

class AndroidNotificationAdapter(
    private val context: Context,
) : NotificationPort {

    companion object {
        private const val CHANNEL_ID = "sensor_alerts"
        private const val CHANNEL_NAME = "Sensor Alerts"
        private const val CHANNEL_DESCRIPTION = "Critical alerts from smart home sensors"
    }

    init {
        createNotificationChannel()
    }

    override fun showSensorAlert(event: DeviceEvent) {
        val (title, icon) = titleAndIconFor(event.type)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(event.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use event id hashCode so each event gets its own notification
        notificationManager.notify(event.id.hashCode(), notification)
    }

    private fun titleAndIconFor(type: DeviceEventType): Pair<String, Int> = when (type) {
        DeviceEventType.SMOKE_ALERT -> "Smoke Alert" to android.R.drawable.ic_dialog_alert
        DeviceEventType.WATER_LEAK_ALERT -> "Water Leak Alert" to android.R.drawable.ic_dialog_alert
        DeviceEventType.DOOR_OPEN_TOO_LONG -> "Door Open Alert" to android.R.drawable.ic_lock_idle_alarm
        else -> "Sensor Alert" to android.R.drawable.ic_dialog_info
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
