package com.vpedrosa.smarthome.device.application

import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.shared.domain.DeviceRepository
import kotlinx.coroutines.flow.first

class ToggleCastingUseCase(
    private val deviceRepository: DeviceRepository,
) {
    suspend operator fun invoke(deviceId: DeviceId) {
        val device = deviceRepository.observeDevice(deviceId).first() ?: return
        if (device !is SmartTv) return
        deviceRepository.save(device.toggleCasting())
    }
}
