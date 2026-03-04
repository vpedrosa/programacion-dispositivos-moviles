package com.vpedrosa.smarthome.device.domain.usecases

import com.vpedrosa.smarthome.device.domain.Blind
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.ports.DeviceRepository
import kotlinx.coroutines.flow.first

class UpdateBlindUseCase(
    private val deviceRepository: DeviceRepository,
) {
    suspend operator fun invoke(id: DeviceId, openingLevel: Int) {
        val device = deviceRepository.observeDevice(id).first() ?: return
        if (device !is Blind) return

        deviceRepository.save(device.changeOpeningLevel(openingLevel))
    }
}
