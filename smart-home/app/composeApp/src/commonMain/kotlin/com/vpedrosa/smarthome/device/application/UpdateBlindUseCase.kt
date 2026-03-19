package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
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
