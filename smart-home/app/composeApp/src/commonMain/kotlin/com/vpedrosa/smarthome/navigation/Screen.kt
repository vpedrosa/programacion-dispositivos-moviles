package com.vpedrosa.smarthome.navigation

import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Dashboard

@Serializable
object Rooms

@Serializable
data class EditGroup(val roomId: String? = null)

@Serializable
object Devices

@Serializable
data class DeviceDetail(val deviceId: String)

@Serializable
object Notifications

@Serializable
object AntiSquatter

@Serializable
object VoiceControl

@Serializable
object Commissioning

@Serializable
object Settings
