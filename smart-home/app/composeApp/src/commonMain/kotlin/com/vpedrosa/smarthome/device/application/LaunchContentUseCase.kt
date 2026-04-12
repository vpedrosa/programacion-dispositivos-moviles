package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.SmartTv
import com.vpedrosa.smarthome.device.domain.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import kotlinx.coroutines.flow.first

class LaunchContentUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(deviceId: DeviceId, url: String) {
        val device = deviceRepository.observeDevice(deviceId).first() ?: return
        if (device !is SmartTv) return

        deviceControlPort.launchContent(deviceId, url)
        deviceRepository.save(device.launchContent(url))
    }
}
