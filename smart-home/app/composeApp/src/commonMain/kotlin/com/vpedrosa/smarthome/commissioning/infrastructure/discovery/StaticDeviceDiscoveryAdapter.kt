package com.vpedrosa.smarthome.commissioning.infrastructure.discovery

import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.commissioning.domain.DeviceDiscoveryPort
import com.vpedrosa.smarthome.commissioning.domain.SimulatorHostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Adaptador de descubrimiento estático para entornos de desarrollo (emulador).
 *
 * Devuelve la lista fija de los 27 dispositivos de la simulación matter.js.
 * Usa la IP almacenada en SimulatorHostRepository si existe, o el fallback
 * del emulador (10.0.2.2) si no se ha descubierto ningún host.
 */
class StaticDeviceDiscoveryAdapter(
    private val simulatorHostRepository: SimulatorHostRepository,
) : DeviceDiscoveryPort {

    override fun discoverDevices(): Flow<List<DiscoveredDevice>> =
        simulatorHostRepository.observeHost().map { host ->
            buildDeviceList(host ?: EMULATOR_HOST)
        }

    companion object {
        const val EMULATOR_HOST = "10.0.2.2"
        const val BASE_PORT = 5540
        const val BASE_DISCRIMINATOR = 3840
        const val BASE_PASSCODE = 20202021L

        fun buildDeviceList(host: String): List<DiscoveredDevice> = buildList {
            var idx = 0
            fun add(type: DeviceType, name: String) {
                add(
                    DiscoveredDevice(
                        name = name,
                        type = type,
                        host = host,
                        port = BASE_PORT + idx,
                        discriminator = BASE_DISCRIMINATOR + idx,
                        passcode = BASE_PASSCODE + idx,
                        serialNumber = "SIM-${(idx + 1).toString().padStart(3, '0')}",
                    ),
                )
                idx++
            }

            // Bombillas (10)
            add(DeviceType.LIGHT, "Bombilla Salón 1")
            add(DeviceType.LIGHT, "Bombilla Salón 2")
            add(DeviceType.LIGHT, "Bombilla Salón 3")
            add(DeviceType.LIGHT, "Bombilla Cocina 1")
            add(DeviceType.LIGHT, "Bombilla Cocina 2")
            add(DeviceType.LIGHT, "Bombilla Dormitorio 1")
            add(DeviceType.LIGHT, "Bombilla Dormitorio 2")
            add(DeviceType.LIGHT, "Bombilla Baño")
            add(DeviceType.LIGHT, "Bombilla Garaje")
            add(DeviceType.LIGHT, "Bombilla Pasillo")
            // Interruptores (5)
            add(DeviceType.SWITCH, "Interruptor Salón")
            add(DeviceType.SWITCH, "Interruptor Cocina")
            add(DeviceType.SWITCH, "Interruptor Dormitorio")
            add(DeviceType.SWITCH, "Interruptor Baño")
            add(DeviceType.SWITCH, "Interruptor Garaje")
            // Cerraduras (2)
            add(DeviceType.LOCK, "Cerradura Entrada")
            add(DeviceType.LOCK, "Cerradura Garaje")
            // Sensor de contacto (1)
            add(DeviceType.CONTACT_SENSOR, "Sensor Contacto Entrada")
            // Persianas (4)
            add(DeviceType.BLIND, "Persiana Salón")
            add(DeviceType.BLIND, "Persiana Cocina")
            add(DeviceType.BLIND, "Persiana Dormitorio")
            add(DeviceType.BLIND, "Persiana Baño")
            // Smart TV (1)
            add(DeviceType.SMART_TV, "Smart TV Salón")
            // Sensor de humo (1)
            add(DeviceType.SMOKE_SENSOR, "Sensor de Humo")
            // Sensor de fugas de agua (1)
            add(DeviceType.WATER_LEAK_SENSOR, "Sensor Fugas Agua")
            // Sensor de temperatura (1)
            add(DeviceType.TEMPERATURE_SENSOR, "Sensor Temperatura")
            // Termostato (1)
            add(DeviceType.THERMOSTAT, "Termostato")
        }
    }
}
