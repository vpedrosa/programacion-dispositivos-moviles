package com.vpedrosa.smarthome.device.adapters.persistence

import com.vpedrosa.smarthome.device.domain.Blind
import com.vpedrosa.smarthome.device.domain.Color
import com.vpedrosa.smarthome.device.domain.ContactSensor
import com.vpedrosa.smarthome.device.domain.Device
import com.vpedrosa.smarthome.device.domain.DeviceId
import com.vpedrosa.smarthome.device.domain.Light
import com.vpedrosa.smarthome.device.domain.Lock
import com.vpedrosa.smarthome.device.domain.Room
import com.vpedrosa.smarthome.device.domain.RoomId
import com.vpedrosa.smarthome.device.domain.SmartTv
import com.vpedrosa.smarthome.device.domain.SmokeSensor
import com.vpedrosa.smarthome.device.domain.Switch
import com.vpedrosa.smarthome.device.domain.TemperatureSensor
import com.vpedrosa.smarthome.device.domain.Thermostat
import com.vpedrosa.smarthome.device.domain.WaterLeakSensor

object DefaultDeviceData {

    // --- Room IDs ---
    private val salonId = RoomId("room-salon")
    private val dormitorioId = RoomId("room-dormitorio")
    private val cocinaId = RoomId("room-cocina")
    private val garajeId = RoomId("room-garaje")

    // --- Lights (10) ---
    private val lightSalon1 = Light(
        id = DeviceId("light-salon-1"),
        name = "Bombilla Salon 1",
        roomId = salonId.value,
        isOn = true,
        color = Color.WARM_WHITE,
        brightness = 80,
    )
    private val lightSalon2 = Light(
        id = DeviceId("light-salon-2"),
        name = "Bombilla Salon 2",
        roomId = salonId.value,
        isOn = false,
        color = Color.WHITE,
        brightness = 50,
    )
    private val lightSalon3 = Light(
        id = DeviceId("light-salon-3"),
        name = "Bombilla Salon 3",
        roomId = salonId.value,
        isOn = true,
        color = Color.WARM_WHITE,
        brightness = 70,
    )
    private val lightDormitorio1 = Light(
        id = DeviceId("light-dormitorio-1"),
        name = "Bombilla Dormitorio 1",
        roomId = dormitorioId.value,
        isOn = false,
        color = Color.WARM_WHITE,
        brightness = 40,
    )
    private val lightDormitorio2 = Light(
        id = DeviceId("light-dormitorio-2"),
        name = "Bombilla Dormitorio 2",
        roomId = dormitorioId.value,
        isOn = false,
        color = Color.WHITE,
        brightness = 60,
    )
    private val lightDormitorio3 = Light(
        id = DeviceId("light-dormitorio-3"),
        name = "Bombilla Dormitorio 3",
        roomId = dormitorioId.value,
        isOn = true,
        color = Color.BLUE,
        brightness = 30,
    )
    private val lightCocina1 = Light(
        id = DeviceId("light-cocina-1"),
        name = "Bombilla Cocina 1",
        roomId = cocinaId.value,
        isOn = true,
        color = Color.WHITE,
        brightness = 100,
    )
    private val lightCocina2 = Light(
        id = DeviceId("light-cocina-2"),
        name = "Bombilla Cocina 2",
        roomId = cocinaId.value,
        isOn = false,
        color = Color.WHITE,
        brightness = 100,
    )
    private val lightGaraje1 = Light(
        id = DeviceId("light-garaje-1"),
        name = "Bombilla Garaje 1",
        roomId = garajeId.value,
        isOn = false,
        color = Color.WHITE,
        brightness = 100,
    )
    private val lightGaraje2 = Light(
        id = DeviceId("light-garaje-2"),
        name = "Bombilla Garaje 2",
        roomId = garajeId.value,
        isOn = false,
        color = Color.WHITE,
        brightness = 100,
    )

    // --- Locks (2) ---
    private val lockPrincipal = Lock(
        id = DeviceId("lock-principal"),
        name = "Cerradura Principal",
        roomId = salonId.value,
        isLocked = true,
    )
    private val lockGaraje = Lock(
        id = DeviceId("lock-garaje"),
        name = "Cerradura Garaje",
        roomId = garajeId.value,
        isLocked = true,
    )

    // --- Blinds (4) ---
    private val blindSalon1 = Blind(
        id = DeviceId("blind-salon-1"),
        name = "Persiana Salon 1",
        roomId = salonId.value,
        openingLevel = 100,
    )
    private val blindSalon2 = Blind(
        id = DeviceId("blind-salon-2"),
        name = "Persiana Salon 2",
        roomId = salonId.value,
        openingLevel = 75,
    )
    private val blindDormitorio1 = Blind(
        id = DeviceId("blind-dormitorio-1"),
        name = "Persiana Dormitorio 1",
        roomId = dormitorioId.value,
        openingLevel = 0,
    )
    private val blindDormitorio2 = Blind(
        id = DeviceId("blind-dormitorio-2"),
        name = "Persiana Dormitorio 2",
        roomId = dormitorioId.value,
        openingLevel = 50,
    )

    // --- Switches (5) ---
    private val switchSalon = Switch(
        id = DeviceId("switch-salon"),
        name = "Interruptor Salon",
        roomId = salonId.value,
        isOn = true,
    )
    private val switchDormitorio = Switch(
        id = DeviceId("switch-dormitorio"),
        name = "Interruptor Dormitorio",
        roomId = dormitorioId.value,
        isOn = false,
    )
    private val switchCocina = Switch(
        id = DeviceId("switch-cocina"),
        name = "Interruptor Cocina",
        roomId = cocinaId.value,
        isOn = true,
    )
    private val switchGaraje = Switch(
        id = DeviceId("switch-garaje"),
        name = "Interruptor Garaje",
        roomId = garajeId.value,
        isOn = false,
    )
    private val switchPasillo = Switch(
        id = DeviceId("switch-pasillo"),
        name = "Interruptor Pasillo",
        roomId = null,
        isOn = true,
    )

    // --- Sensors ---
    private val smokeSensor = SmokeSensor(
        id = DeviceId("smoke-sensor-cocina"),
        name = "Sensor de Humo",
        roomId = cocinaId.value,
        isSmokeDetected = false,
    )
    private val waterLeakSensor = WaterLeakSensor(
        id = DeviceId("water-leak-sensor-cocina"),
        name = "Sensor de Fugas",
        roomId = cocinaId.value,
        isLeakDetected = false,
    )
    private val temperatureSensor = TemperatureSensor(
        id = DeviceId("temp-sensor-salon"),
        name = "Sensor de Temperatura",
        roomId = salonId.value,
        currentTemperature = 22.5,
    )
    private val contactSensor = ContactSensor(
        id = DeviceId("contact-sensor-puerta"),
        name = "Sensor de Contacto Puerta",
        roomId = salonId.value,
        isOpen = false,
    )

    // --- Thermostat ---
    private val thermostat = Thermostat(
        id = DeviceId("thermostat-salon"),
        name = "Termostato",
        roomId = salonId.value,
        currentTemperature = 22.5,
        targetTemperature = 23.0,
        isHeatingOn = true,
    )

    // --- Smart TV ---
    private val smartTv = SmartTv(
        id = DeviceId("smart-tv-salon"),
        name = "Smart TV Salon",
        roomId = salonId.value,
        isOn = false,
        isCasting = false,
    )

    // --- All devices ---
    val devices: List<Device> = listOf(
        lightSalon1, lightSalon2, lightSalon3,
        lightDormitorio1, lightDormitorio2, lightDormitorio3,
        lightCocina1, lightCocina2,
        lightGaraje1, lightGaraje2,
        lockPrincipal, lockGaraje,
        blindSalon1, blindSalon2,
        blindDormitorio1, blindDormitorio2,
        switchSalon, switchDormitorio, switchCocina, switchGaraje, switchPasillo,
        smokeSensor, waterLeakSensor, temperatureSensor, contactSensor,
        thermostat,
        smartTv,
    )

    // --- Rooms ---
    val rooms: List<Room> = listOf(
        Room(
            id = salonId,
            name = "Salon",
            photoUri = null,
            deviceIds = devices.filter { it.roomId == salonId.value }.map { it.id },
        ),
        Room(
            id = dormitorioId,
            name = "Dormitorio",
            photoUri = null,
            deviceIds = devices.filter { it.roomId == dormitorioId.value }.map { it.id },
        ),
        Room(
            id = cocinaId,
            name = "Cocina",
            photoUri = null,
            deviceIds = devices.filter { it.roomId == cocinaId.value }.map { it.id },
        ),
        Room(
            id = garajeId,
            name = "Garaje",
            photoUri = null,
            deviceIds = devices.filter { it.roomId == garajeId.value }.map { it.id },
        ),
    )
}
