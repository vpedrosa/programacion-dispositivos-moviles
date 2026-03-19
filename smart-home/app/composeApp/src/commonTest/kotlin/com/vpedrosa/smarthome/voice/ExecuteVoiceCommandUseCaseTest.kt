package com.vpedrosa.smarthome.voice

import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.shared.infrastructure.persistence.InMemoryRoomRepository
import com.vpedrosa.smarthome.shared.domain.model.Blind
import com.vpedrosa.smarthome.shared.domain.model.Color
import com.vpedrosa.smarthome.shared.domain.model.DeviceId
import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.Light
import com.vpedrosa.smarthome.shared.domain.model.Lock
import com.vpedrosa.smarthome.voice.domain.model.ParsedVoiceCommand
import com.vpedrosa.smarthome.shared.domain.model.Room
import com.vpedrosa.smarthome.shared.domain.model.RoomId
import com.vpedrosa.smarthome.shared.domain.model.SmartTv
import com.vpedrosa.smarthome.shared.domain.model.Switch
import com.vpedrosa.smarthome.shared.domain.model.Thermostat
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.shared.domain.DeviceControlPort
import com.vpedrosa.smarthome.voice.application.ExecuteVoiceCommandUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FakeVoiceDeviceControlPort : DeviceControlPort {
    override fun registerDevice(deviceId: DeviceId, discoveredDevice: DiscoveredDevice) {}
    override suspend fun toggleOnOff(deviceId: DeviceId, on: Boolean) {}
    override suspend fun setLevel(deviceId: DeviceId, level: Int) {}
    override suspend fun lockDoor(deviceId: DeviceId, lock: Boolean) {}
    override suspend fun setThermostatSetpoint(deviceId: DeviceId, temperatureCelsius: Double) {}
    override suspend fun setThermostatMode(deviceId: DeviceId, heating: Boolean) {}
    override suspend fun setWindowCoveringPosition(deviceId: DeviceId, openPercent: Int) {}
}

class ExecuteVoiceCommandUseCaseTest {

    private val deviceRepo = InMemoryDeviceRepository(initialDevices = emptyList())
    private val roomRepo = InMemoryRoomRepository(initialRooms = emptyList())
    private val execute = ExecuteVoiceCommandUseCase(deviceRepo, roomRepo, FakeVoiceDeviceControlPort())

    // -- Toggle lights --

    @Test
    fun turnOnAllLights() = runTest {
        val light1 = Light(DeviceId("l1"), "Lamp 1", RoomId("room-1"), isOn = false, Color.WHITE, 80)
        val light2 = Light(DeviceId("l2"), "Lamp 2", RoomId("room-1"), isOn = false, Color.WHITE, 60)
        deviceRepo.save(light1)
        deviceRepo.save(light2)

        val result = execute(ParsedVoiceCommand.ToggleDevices(DeviceType.LIGHT, turnOn = true))

        assertTrue(result.success)
        assertEquals(2, result.devicesAffected)

        val devices = deviceRepo.observeAllDevices().first()
        assertTrue(devices.all { (it as Light).isOn })
    }

    @Test
    fun turnOnLightsInSpecificRoom() = runTest {
        val light1 = Light(DeviceId("l1"), "Lamp 1", RoomId("room-1"), isOn = false, Color.WHITE, 80)
        val light2 = Light(DeviceId("l2"), "Lamp 2", RoomId("room-2"), isOn = false, Color.WHITE, 60)
        deviceRepo.save(light1)
        deviceRepo.save(light2)

        val room = Room(RoomId("room-1"), "Salon", null, listOf(DeviceId("l1")))
        roomRepo.save(room)

        val result = execute(ParsedVoiceCommand.ToggleDevices(DeviceType.LIGHT, turnOn = true, roomName = "salon"))

        assertTrue(result.success)
        assertEquals(1, result.devicesAffected)

        val l1 = deviceRepo.observeDevice(DeviceId("l1")).first() as Light
        val l2 = deviceRepo.observeDevice(DeviceId("l2")).first() as Light
        assertTrue(l1.isOn)
        assertTrue(!l2.isOn)
    }

    // -- Toggle TV --

    @Test
    fun turnOffTv() = runTest {
        val tv = SmartTv(DeviceId("tv1"), "Smart TV", RoomId("room-1"), isOn = true, isCasting = false)
        deviceRepo.save(tv)

        val result = execute(ParsedVoiceCommand.ToggleDevices(DeviceType.SMART_TV, turnOn = false))

        assertTrue(result.success)
        assertEquals(1, result.devicesAffected)

        val updated = deviceRepo.observeDevice(DeviceId("tv1")).first() as SmartTv
        assertTrue(!updated.isOn)
    }

    // -- Blinds --

    @Test
    fun openBlinds() = runTest {
        val blind = Blind(DeviceId("b1"), "Blind 1", RoomId("room-1"), openingLevel = 0)
        deviceRepo.save(blind)

        val result = execute(ParsedVoiceCommand.SetBlinds(open = true))

        assertTrue(result.success)
        assertEquals(1, result.devicesAffected)

        val updated = deviceRepo.observeDevice(DeviceId("b1")).first() as Blind
        assertEquals(100, updated.openingLevel)
    }

    @Test
    fun closeBlinds() = runTest {
        val blind = Blind(DeviceId("b1"), "Blind 1", RoomId("room-1"), openingLevel = 80)
        deviceRepo.save(blind)

        val result = execute(ParsedVoiceCommand.SetBlinds(open = false))

        assertTrue(result.success)
        val updated = deviceRepo.observeDevice(DeviceId("b1")).first() as Blind
        assertEquals(0, updated.openingLevel)
    }

    // -- Thermostat --

    @Test
    fun setThermostatTemperature() = runTest {
        val thermostat = Thermostat(
            DeviceId("t1"), "Thermostat", RoomId("room-1"),
            currentTemperature = 20.0, targetTemperature = 18.0, isHeatingOn = true,
        )
        deviceRepo.save(thermostat)

        val result = execute(ParsedVoiceCommand.SetThermostat(targetTemperature = 24.0))

        assertTrue(result.success)
        assertEquals(1, result.devicesAffected)

        val updated = deviceRepo.observeDevice(DeviceId("t1")).first() as Thermostat
        assertEquals(24.0, updated.targetTemperature)
    }

    // -- Lock --

    @Test
    fun lockDoor() = runTest {
        val lock = Lock(DeviceId("lk1"), "Front Door", RoomId("room-1"), isLocked = false)
        deviceRepo.save(lock)

        val result = execute(ParsedVoiceCommand.ToggleLock(lock = true))

        assertTrue(result.success)
        assertEquals(1, result.devicesAffected)

        val updated = deviceRepo.observeDevice(DeviceId("lk1")).first() as Lock
        assertTrue(updated.isLocked)
    }

    @Test
    fun unlockDoorByName() = runTest {
        val lock1 = Lock(DeviceId("lk1"), "Front Door", RoomId("room-1"), isLocked = true)
        val lock2 = Lock(DeviceId("lk2"), "Garage Door", RoomId("room-2"), isLocked = true)
        deviceRepo.save(lock1)
        deviceRepo.save(lock2)

        val result = execute(ParsedVoiceCommand.ToggleLock(lock = false, doorName = "garage"))

        assertTrue(result.success)
        assertEquals(1, result.devicesAffected)

        val lk1 = deviceRepo.observeDevice(DeviceId("lk1")).first() as Lock
        val lk2 = deviceRepo.observeDevice(DeviceId("lk2")).first() as Lock
        assertTrue(lk1.isLocked) // unchanged
        assertTrue(!lk2.isLocked) // unlocked
    }

    // -- Unknown command --

    @Test
    fun unknownCommandReturnsFalse() = runTest {
        val result = execute(ParsedVoiceCommand.Unknown)
        assertTrue(!result.success)
        assertEquals(0, result.devicesAffected)
    }

    // -- No devices found --

    @Test
    fun noDevicesFoundReturnsFalse() = runTest {
        val result = execute(ParsedVoiceCommand.ToggleDevices(DeviceType.LIGHT, turnOn = true))
        assertTrue(!result.success)
        assertEquals(0, result.devicesAffected)
    }
}
