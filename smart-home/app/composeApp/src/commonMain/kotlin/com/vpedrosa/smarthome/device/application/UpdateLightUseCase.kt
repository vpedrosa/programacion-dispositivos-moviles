package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.device.domain.model.Color
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.Light
import com.vpedrosa.smarthome.device.domain.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import kotlinx.coroutines.flow.first

class UpdateLightUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(
        id: DeviceId,
        color: Color? = null,
        brightness: Int? = null,
    ) {
        val device = deviceRepository.observeDevice(id).first() ?: return
        if (device !is Light) return

        var updated = device
        if (color != null) {
            deviceControlPort.setColor(id, color)
            updated = updated.changeColor(color)
        }
        if (brightness != null) {
            deviceControlPort.setLevel(id, brightness)
            updated = updated.changeBrightness(brightness)
        }

        deviceRepository.save(updated)
    }
}
