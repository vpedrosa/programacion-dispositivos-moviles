package com.vpedrosa.smarthome.commissioning.application

import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice

class FindDiscoveredDeviceByQrUseCase {
    operator fun invoke(
        devices: List<DiscoveredDevice>,
        discriminator: Int,
        passcode: Long,
    ): DiscoveredDevice? = devices.find {
        it.discriminator == discriminator && it.passcode == passcode
    }
}
