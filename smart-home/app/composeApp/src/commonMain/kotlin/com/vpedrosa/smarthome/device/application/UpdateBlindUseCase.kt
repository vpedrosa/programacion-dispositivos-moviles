package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.device.domain.model.Blind
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.DeviceControlPort
import com.vpedrosa.smarthome.device.domain.DeviceRepository
import kotlinx.coroutines.flow.first

class UpdateBlindUseCase(
    private val deviceRepository: DeviceRepository,
    private val deviceControlPort: DeviceControlPort,
) {
    suspend operator fun invoke(id: DeviceId, openingLevel: Int) {
        val device = deviceRepository.observeDevice(id).first() ?: return
        if (device !is Blind) return

        deviceControlPort.setWindowCoveringPosition(id, openingLevel)
        deviceRepository.save(device.changeOpeningLevel(openingLevel))
    }
}
