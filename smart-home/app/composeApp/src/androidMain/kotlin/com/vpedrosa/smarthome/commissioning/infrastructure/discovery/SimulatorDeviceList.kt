package com.vpedrosa.smarthome.commissioning.infrastructure.discovery

import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.device.domain.model.DeviceType

/**
 * Espejo de simulation/src/config.mjs.
 *
 * Parámetros generados con la misma lógica que el script Node:
 *   port        = BASE_PORT + idx
 *   discriminator = BASE_DISCRIMINATOR + idx
 *   passcode    = BASE_PASSCODE + idx
 *   serialNumber = "SIM-001" … "SIM-027"
 *
 * Se usa como fallback cuando mDNS no funciona (emulador Android).
 */
internal object SimulatorDeviceList {

    private const val BASE_PORT = 5540
    private const val BASE_DISCRIMINATOR = 3840
    private const val BASE_PASSCODE = 20202021L

    // Mismo orden que config.mjs: (type, name)
    private val defs = listOf(
        // Bombillas (10) — idx 0-9
        DeviceType.LIGHT to "Bombilla Salon 1",
        DeviceType.LIGHT to "Bombilla Salon 2",
        DeviceType.LIGHT to "Bombilla Salon 3",
        DeviceType.LIGHT to "Bombilla Cocina 1",
        DeviceType.LIGHT to "Bombilla Cocina 2",
        DeviceType.LIGHT to "Bombilla Dormitorio 1",
        DeviceType.LIGHT to "Bombilla Dormitorio 2",
        DeviceType.LIGHT to "Bombilla Bano",
        DeviceType.LIGHT to "Bombilla Garaje",
        DeviceType.LIGHT to "Bombilla Pasillo",
        // Interruptores (5) — idx 10-14
        DeviceType.SWITCH to "Interruptor Salon",
        DeviceType.SWITCH to "Interruptor Cocina",
        DeviceType.SWITCH to "Interruptor Dormitorio",
        DeviceType.SWITCH to "Interruptor Bano",
        DeviceType.SWITCH to "Interruptor Garaje",
        // Cerraduras (2) — idx 15-16
        DeviceType.LOCK to "Cerradura Entrada",
        DeviceType.LOCK to "Cerradura Garaje",
        // Sensor de contacto (1) — idx 17
        DeviceType.CONTACT_SENSOR to "Sensor Contacto Entrada",
        // Persianas (4) — idx 18-21
        DeviceType.BLIND to "Persiana Salon",
        DeviceType.BLIND to "Persiana Cocina",
        DeviceType.BLIND to "Persiana Dormitorio",
        DeviceType.BLIND to "Persiana Bano",
        // Smart TV (1) — idx 22
        DeviceType.SMART_TV to "Smart TV Salon",
        // Sensor de humo (1) — idx 23
        DeviceType.SMOKE_SENSOR to "Sensor de Humo",
        // Sensor de fugas (1) — idx 24
        DeviceType.WATER_LEAK_SENSOR to "Sensor Fugas Agua",
        // Sensor de temperatura (1) — idx 25
        DeviceType.TEMPERATURE_SENSOR to "Sensor Temperatura",
        // Termostato (1) — idx 26
        DeviceType.THERMOSTAT to "Termostato",
    )

    fun forHost(host: String): List<DiscoveredDevice> =
        defs.mapIndexed { idx, (type, name) ->
            DiscoveredDevice(
                name = name,
                type = type,
                host = host,
                port = BASE_PORT + idx,
                discriminator = BASE_DISCRIMINATOR + idx,
                passcode = BASE_PASSCODE + idx,
                serialNumber = "SIM-${(idx + 1).toString().padStart(3, '0')}",
            )
        }
}
