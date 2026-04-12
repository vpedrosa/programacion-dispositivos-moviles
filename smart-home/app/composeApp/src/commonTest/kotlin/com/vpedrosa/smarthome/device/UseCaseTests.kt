package com.vpedrosa.smarthome.device

import com.vpedrosa.smarthome.device.infrastructure.persistence.InMemoryDeviceEventRepository
import com.vpedrosa.smarthome.device.infrastructure.persistence.InMemoryDeviceRepository
import com.vpedrosa.smarthome.room.infrastructure.InMemoryRoomRepository
import com.vpedrosa.smarthome.device.domain.model.Blind
import com.vpedrosa.smarthome.device.domain.model.Color
import com.vpedrosa.smarthome.device.domain.model.DeviceId
import com.vpedrosa.smarthome.device.domain.model.DeviceType
import com.vpedrosa.smarthome.device.domain.model.Light
import com.vpedrosa.smarthome.device.domain.model.Lock
import com.vpedrosa.smarthome.room.domain.model.Room
import com.vpedrosa.smarthome.device.domain.model.RoomId
import com.vpedrosa.smarthome.device.domain.model.SmartTv
import com.vpedrosa.smarthome.device.domain.model.SmokeSensor
import com.vpedrosa.smarthome.device.domain.model.Switch
import com.vpedrosa.smarthome.device.domain.model.Thermostat
import com.vpedrosa.smarthome.device.domain.model.DeviceConnectionInfo
import com.vpedrosa.smarthome.device.domain.DeviceControlPort
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeInRoomUseCase
import com.vpedrosa.smarthome.device.application.BulkToggleDevicesByTypeUseCase
import com.vpedrosa.smarthome.device.application.LockDoorUseCase
import com.vpedrosa.smarthome.device.application.ToggleDeviceUseCase
import com.vpedrosa.smarthome.device.application.UpdateBlindUseCase
import com.vpedrosa.smarthome.device.application.UpdateLightUseCase
import com.vpedrosa.smarthome.device.application.UpdateThermostatUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class FakeDeviceControlPort : DeviceControlPort {
    override fun registerDevice(deviceId: DeviceId, connectionInfo: DeviceConnectionInfo) {}
    override fun deregisterDevice(deviceId: DeviceId) {}
    override suspend fun toggleOnOff(deviceId: DeviceId, on: Boolean) {}
    override suspend fun setLevel(deviceId: DeviceId, level: Int) {}
    override suspend fun setColor(deviceId: DeviceId, color: Color) {}
    override suspend fun lockDoor(deviceId: DeviceId, lock: Boolean) {}
    override suspend fun setThermostatSetpoint(deviceId: DeviceId, temperatureCelsius: Double) {}
    override suspend fun setThermostatMode(deviceId: DeviceId, heating: Boolean) {}
    override suspend fun setWindowCoveringPosition(deviceId: DeviceId, openPercent: Int) {}
    override suspend fun launchContent(deviceId: DeviceId, url: String) {}
}

// region ToggleDeviceUseCase

class ToggleDeviceUseCaseTest {

    private val light = Light(
        id = DeviceId("light-1"),
        name = "Lamp",
        roomId = RoomId("room-1"),
        isOn = false,
        color = Color.WHITE,
        brightness = 80,
    )
    private val lock = Lock(
        id = DeviceId("lock-1"),
        name = "Front Door",
        roomId = RoomId("room-1"),
        isLocked = true,
    )
    private val switch = Switch(
        id = DeviceId("switch-1"),
        name = "Switch",
        roomId = RoomId("room-1"),
        isOn = false,
    )
    private val smartTv = SmartTv(
        id = DeviceId("tv-1"),
        name = "TV",
        roomId = RoomId("room-1"),
        isOn = false,
        contentUrl = null,
    )
    private val thermostat = Thermostat(
        id = DeviceId("therm-1"),
        name = "Thermostat",
        roomId = RoomId("room-1"),
        currentTemperature = 20.0,
        targetTemperature = 22.0,
        isHeatingOn = false,
    )
    private val blind = Blind(
        id = DeviceId("blind-1"),
        name = "Blind",
        roomId = RoomId("room-1"),
        openingLevel = 50,
    )
    private val smokeSensor = SmokeSensor(
        id = DeviceId("smoke-1"),
        name = "Smoke Sensor",
        roomId = RoomId("room-1"),
        isSmokeDetected = false,
    )

    @Test
    fun toggleLight_turnsOnWhenOff() = runTest {
        val repo = InMemoryDeviceRepository(listOf(light))
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(light.id)

        val result = repo.observeDevice(light.id).first() as Light
        assertTrue(result.isOn)
    }

    @Test
    fun toggleLight_turnsOffWhenOn() = runTest {
        val repo = InMemoryDeviceRepository(listOf(light.copy(isOn = true)))
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(light.id)

        val result = repo.observeDevice(light.id).first() as Light
        assertFalse(result.isOn)
    }

    @Test
    fun toggleLock_unlocksWhenLocked() = runTest {
        val repo = InMemoryDeviceRepository(listOf(lock))
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(lock.id)

        val result = repo.observeDevice(lock.id).first() as Lock
        assertFalse(result.isLocked)
    }

    @Test
    fun toggleLock_locksWhenUnlocked() = runTest {
        val repo = InMemoryDeviceRepository(listOf(lock.copy(isLocked = false)))
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(lock.id)

        val result = repo.observeDevice(lock.id).first() as Lock
        assertTrue(result.isLocked)
    }

    @Test
    fun toggleSwitch_turnsOnWhenOff() = runTest {
        val repo = InMemoryDeviceRepository(listOf(switch))
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(switch.id)

        val result = repo.observeDevice(switch.id).first() as Switch
        assertTrue(result.isOn)
    }

    @Test
    fun toggleSmartTv_turnsOnWhenOff() = runTest {
        val repo = InMemoryDeviceRepository(listOf(smartTv))
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(smartTv.id)

        val result = repo.observeDevice(smartTv.id).first() as SmartTv
        assertTrue(result.isOn)
    }

    @Test
    fun toggleThermostat_togglesHeating() = runTest {
        val repo = InMemoryDeviceRepository(listOf(thermostat))
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(thermostat.id)

        val result = repo.observeDevice(thermostat.id).first() as Thermostat
        assertTrue(result.isHeatingOn)
    }

    @Test
    fun toggleBlind_doesNothing() = runTest {
        val repo = InMemoryDeviceRepository(listOf(blind))
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(blind.id)

        val result = repo.observeDevice(blind.id).first() as Blind
        assertEquals(50, result.openingLevel, "Blind should remain unchanged after toggle")
    }

    @Test
    fun toggleSensor_doesNothing() = runTest {
        val repo = InMemoryDeviceRepository(listOf(smokeSensor))
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(smokeSensor.id)

        val result = repo.observeDevice(smokeSensor.id).first() as SmokeSensor
        assertFalse(result.isSmokeDetected, "Smoke sensor should remain unchanged after toggle")
    }

    @Test
    fun toggleNonexistentDevice_doesNotCrash() = runTest {
        val repo = InMemoryDeviceRepository(emptyList())
        val useCase = ToggleDeviceUseCase(repo, FakeDeviceControlPort(), InMemoryDeviceEventRepository())

        useCase(DeviceId("nonexistent"))

        // No exception = success
        assertTrue(repo.observeAllDevices().first().isEmpty())
    }
}

// endregion

// region UpdateLightUseCase

class UpdateLightUseCaseTest {

    private val light = Light(
        id = DeviceId("light-1"),
        name = "Lamp",
        roomId = RoomId("room-1"),
        isOn = true,
        color = Color.WHITE,
        brightness = 50,
    )

    @Test
    fun updateColor_changesColorOnly() = runTest {
        val repo = InMemoryDeviceRepository(listOf(light))
        val useCase = UpdateLightUseCase(repo, FakeDeviceControlPort())

        useCase(light.id, color = Color(255, 0, 0))

        val result = repo.observeDevice(light.id).first() as Light
        assertEquals(Color(255, 0, 0), result.color)
        assertEquals(50, result.brightness, "Brightness should remain unchanged")
    }

    @Test
    fun updateBrightness_changesBrightnessOnly() = runTest {
        val repo = InMemoryDeviceRepository(listOf(light))
        val useCase = UpdateLightUseCase(repo, FakeDeviceControlPort())

        useCase(light.id, brightness = 90)

        val result = repo.observeDevice(light.id).first() as Light
        assertEquals(90, result.brightness)
        assertEquals(Color.WHITE, result.color, "Color should remain unchanged")
    }

    @Test
    fun updateBoth_changesColorAndBrightness() = runTest {
        val repo = InMemoryDeviceRepository(listOf(light))
        val useCase = UpdateLightUseCase(repo, FakeDeviceControlPort())

        useCase(light.id, color = Color(0, 255, 0), brightness = 100)

        val result = repo.observeDevice(light.id).first() as Light
        assertEquals(Color(0, 255, 0), result.color)
        assertEquals(100, result.brightness)
    }

    @Test
    fun updateNonLight_doesNothing() = runTest {
        val lock = Lock(DeviceId("lock-1"), "Lock", RoomId("room-1"), isLocked = true)
        val repo = InMemoryDeviceRepository(listOf(lock))
        val useCase = UpdateLightUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceId("lock-1"), brightness = 80)

        val result = repo.observeDevice(DeviceId("lock-1")).first() as Lock
        assertTrue(result.isLocked, "Lock should remain unchanged")
    }

    @Test
    fun updateNonexistentDevice_doesNotCrash() = runTest {
        val repo = InMemoryDeviceRepository(emptyList())
        val useCase = UpdateLightUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceId("nonexistent"), brightness = 80)

        assertTrue(repo.observeAllDevices().first().isEmpty())
    }
}

// endregion

// region UpdateBlindUseCase

class UpdateBlindUseCaseTest {

    private val blind = Blind(
        id = DeviceId("blind-1"),
        name = "Blind",
        roomId = RoomId("room-1"),
        openingLevel = 50,
    )

    @Test
    fun updateOpeningLevel_changesToNewLevel() = runTest {
        val repo = InMemoryDeviceRepository(listOf(blind))
        val useCase = UpdateBlindUseCase(repo, FakeDeviceControlPort())

        useCase(blind.id, openingLevel = 75)

        val result = repo.observeDevice(blind.id).first() as Blind
        assertEquals(75, result.openingLevel)
    }

    @Test
    fun updateOpeningLevel_fullyClosed() = runTest {
        val repo = InMemoryDeviceRepository(listOf(blind))
        val useCase = UpdateBlindUseCase(repo, FakeDeviceControlPort())

        useCase(blind.id, openingLevel = 0)

        val result = repo.observeDevice(blind.id).first() as Blind
        assertEquals(0, result.openingLevel)
    }

    @Test
    fun updateOpeningLevel_fullyOpen() = runTest {
        val repo = InMemoryDeviceRepository(listOf(blind))
        val useCase = UpdateBlindUseCase(repo, FakeDeviceControlPort())

        useCase(blind.id, openingLevel = 100)

        val result = repo.observeDevice(blind.id).first() as Blind
        assertEquals(100, result.openingLevel)
    }

    @Test
    fun updateNonBlind_doesNothing() = runTest {
        val light = Light(DeviceId("light-1"), "Lamp", RoomId("room-1"), isOn = true, Color.WHITE, 80)
        val repo = InMemoryDeviceRepository(listOf(light))
        val useCase = UpdateBlindUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceId("light-1"), openingLevel = 50)

        val result = repo.observeDevice(DeviceId("light-1")).first() as Light
        assertTrue(result.isOn, "Light should remain unchanged")
    }

    @Test
    fun updateNonexistentDevice_doesNotCrash() = runTest {
        val repo = InMemoryDeviceRepository(emptyList())
        val useCase = UpdateBlindUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceId("nonexistent"), openingLevel = 50)

        assertTrue(repo.observeAllDevices().first().isEmpty())
    }
}

// endregion

// region UpdateThermostatUseCase

class UpdateThermostatUseCaseTest {

    private val thermostat = Thermostat(
        id = DeviceId("therm-1"),
        name = "Thermostat",
        roomId = RoomId("room-1"),
        currentTemperature = 20.0,
        targetTemperature = 22.0,
        isHeatingOn = true,
    )

    @Test
    fun updateTargetTemperature_changesTarget() = runTest {
        val repo = InMemoryDeviceRepository(listOf(thermostat))
        val useCase = UpdateThermostatUseCase(repo, FakeDeviceControlPort())

        useCase(thermostat.id, targetTemperature = 25.0)

        val result = repo.observeDevice(thermostat.id).first() as Thermostat
        assertEquals(25.0, result.targetTemperature)
        assertTrue(result.isHeatingOn, "Heating state should remain unchanged")
    }

    @Test
    fun updateHeatingState_changesHeating() = runTest {
        val repo = InMemoryDeviceRepository(listOf(thermostat))
        val useCase = UpdateThermostatUseCase(repo, FakeDeviceControlPort())

        useCase(thermostat.id, isHeatingOn = false)

        val result = repo.observeDevice(thermostat.id).first() as Thermostat
        assertFalse(result.isHeatingOn)
        assertEquals(22.0, result.targetTemperature, "Target temp should remain unchanged")
    }

    @Test
    fun updateBoth_changesTemperatureAndHeating() = runTest {
        val repo = InMemoryDeviceRepository(listOf(thermostat))
        val useCase = UpdateThermostatUseCase(repo, FakeDeviceControlPort())

        useCase(thermostat.id, targetTemperature = 18.0, isHeatingOn = false)

        val result = repo.observeDevice(thermostat.id).first() as Thermostat
        assertEquals(18.0, result.targetTemperature)
        assertFalse(result.isHeatingOn)
    }

    @Test
    fun updateNonThermostat_doesNothing() = runTest {
        val light = Light(DeviceId("light-1"), "Lamp", RoomId("room-1"), isOn = true, Color.WHITE, 80)
        val repo = InMemoryDeviceRepository(listOf(light))
        val useCase = UpdateThermostatUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceId("light-1"), targetTemperature = 25.0)

        val result = repo.observeDevice(DeviceId("light-1")).first() as Light
        assertTrue(result.isOn, "Light should remain unchanged")
    }

    @Test
    fun updateNonexistentDevice_doesNotCrash() = runTest {
        val repo = InMemoryDeviceRepository(emptyList())
        val useCase = UpdateThermostatUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceId("nonexistent"), targetTemperature = 25.0)

        assertTrue(repo.observeAllDevices().first().isEmpty())
    }
}

// endregion

// region BulkToggleDevicesByTypeUseCase

class BulkToggleDevicesByTypeUseCaseTest {

    @Test
    fun turnOnAllLights_setsAllLightsOn() = runTest {
        val light1 = Light(DeviceId("l1"), "L1", RoomId("room-1"), isOn = false, Color.WHITE, 80)
        val light2 = Light(DeviceId("l2"), "L2", RoomId("room-1"), isOn = false, Color.WHITE, 60)
        val light3 = Light(DeviceId("l3"), "L3", RoomId("room-2"), isOn = true, Color.WHITE, 50)
        val repo = InMemoryDeviceRepository(listOf(light1, light2, light3))
        val useCase = BulkToggleDevicesByTypeUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceType.LIGHT, turnOn = true)

        val lights = repo.observeDevicesByType(DeviceType.LIGHT).first()
        assertTrue(lights.all { (it as Light).isOn })
    }

    @Test
    fun turnOffAllLights_setsAllLightsOff() = runTest {
        val light1 = Light(DeviceId("l1"), "L1", RoomId("room-1"), isOn = true, Color.WHITE, 80)
        val light2 = Light(DeviceId("l2"), "L2", RoomId("room-1"), isOn = true, Color.WHITE, 60)
        val repo = InMemoryDeviceRepository(listOf(light1, light2))
        val useCase = BulkToggleDevicesByTypeUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceType.LIGHT, turnOn = false)

        val lights = repo.observeDevicesByType(DeviceType.LIGHT).first()
        assertTrue(lights.all { !(it as Light).isOn })
    }

    @Test
    fun turnOnAllLights_doesNotAffectOtherDeviceTypes() = runTest {
        val light = Light(DeviceId("l1"), "Lamp", RoomId("room-1"), isOn = false, Color.WHITE, 80)
        val lock = Lock(DeviceId("lk1"), "Lock", RoomId("room-1"), isLocked = true)
        val repo = InMemoryDeviceRepository(listOf(light, lock))
        val useCase = BulkToggleDevicesByTypeUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceType.LIGHT, turnOn = true)

        val updatedLock = repo.observeDevice(DeviceId("lk1")).first() as Lock
        assertTrue(updatedLock.isLocked, "Lock should remain unchanged")
    }

    @Test
    fun bulkToggleLocks_locksAll() = runTest {
        val lock1 = Lock(DeviceId("lk1"), "Lock 1", RoomId("room-1"), isLocked = false)
        val lock2 = Lock(DeviceId("lk2"), "Lock 2", RoomId("room-2"), isLocked = false)
        val repo = InMemoryDeviceRepository(listOf(lock1, lock2))
        val useCase = BulkToggleDevicesByTypeUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceType.LOCK, turnOn = true)

        val locks = repo.observeDevicesByType(DeviceType.LOCK).first()
        assertTrue(locks.all { (it as Lock).isLocked })
    }

    @Test
    fun bulkToggleSwitches_turnsOnAll() = runTest {
        val sw1 = Switch(DeviceId("sw1"), "Switch 1", RoomId("room-1"), isOn = false)
        val sw2 = Switch(DeviceId("sw2"), "Switch 2", RoomId("room-1"), isOn = true)
        val repo = InMemoryDeviceRepository(listOf(sw1, sw2))
        val useCase = BulkToggleDevicesByTypeUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceType.SWITCH, turnOn = true)

        val switches = repo.observeDevicesByType(DeviceType.SWITCH).first()
        assertTrue(switches.all { (it as Switch).isOn })
    }

    @Test
    fun bulkToggle_skipsAlreadyMatchingDevices() = runTest {
        val light1 = Light(DeviceId("l1"), "L1", RoomId("room-1"), isOn = true, Color.WHITE, 80)
        val light2 = Light(DeviceId("l2"), "L2", RoomId("room-1"), isOn = true, Color.WHITE, 60)
        val repo = InMemoryDeviceRepository(listOf(light1, light2))
        val useCase = BulkToggleDevicesByTypeUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceType.LIGHT, turnOn = true)

        // All were already on, so nothing should have changed
        val lights = repo.observeDevicesByType(DeviceType.LIGHT).first()
        assertTrue(lights.all { (it as Light).isOn })
    }

    @Test
    fun bulkToggle_noDevicesOfType_doesNotCrash() = runTest {
        val repo = InMemoryDeviceRepository(emptyList())
        val useCase = BulkToggleDevicesByTypeUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceType.LIGHT, turnOn = true)

        assertTrue(repo.observeAllDevices().first().isEmpty())
    }

    @Test
    fun bulkToggleThermostat_controlsHeating() = runTest {
        val t1 = Thermostat(DeviceId("t1"), "Therm", RoomId("room-1"), 20.0, 22.0, isHeatingOn = false)
        val repo = InMemoryDeviceRepository(listOf(t1))
        val useCase = BulkToggleDevicesByTypeUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceType.THERMOSTAT, turnOn = true)

        val result = repo.observeDevice(DeviceId("t1")).first() as Thermostat
        assertTrue(result.isHeatingOn)
    }

    @Test
    fun bulkToggleSmartTv_turnsOnAll() = runTest {
        val tv = SmartTv(DeviceId("tv1"), "TV", RoomId("room-1"), isOn = false)
        val repo = InMemoryDeviceRepository(listOf(tv))
        val useCase = BulkToggleDevicesByTypeUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceType.SMART_TV, turnOn = true)

        val result = repo.observeDevice(DeviceId("tv1")).first() as SmartTv
        assertTrue(result.isOn)
    }
}

// endregion

// region LockDoorUseCase

class LockDoorUseCaseTest {

    private val lock = Lock(DeviceId("lk1"), "Front Door", RoomId("room-1"), isLocked = false)

    @Test
    fun lockDoor_locksAnUnlockedDoor() = runTest {
        val repo = InMemoryDeviceRepository(listOf(lock))
        val useCase = LockDoorUseCase(repo, FakeDeviceControlPort())

        useCase(lock.id, lock = true)

        val result = repo.observeDevice(lock.id).first() as Lock
        assertTrue(result.isLocked)
    }

    @Test
    fun lockDoor_unlocksALockedDoor() = runTest {
        val repo = InMemoryDeviceRepository(listOf(lock.copy(isLocked = true)))
        val useCase = LockDoorUseCase(repo, FakeDeviceControlPort())

        useCase(lock.id, lock = false)

        val result = repo.observeDevice(lock.id).first() as Lock
        assertFalse(result.isLocked)
    }

    @Test
    fun lockDoor_nonLockDevice_doesNothing() = runTest {
        val light = Light(DeviceId("l1"), "Lamp", RoomId("room-1"), isOn = true, Color.WHITE, 80)
        val repo = InMemoryDeviceRepository(listOf(light))
        val useCase = LockDoorUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceId("l1"), lock = true)

        val result = repo.observeDevice(DeviceId("l1")).first() as Light
        assertTrue(result.isOn, "Light should remain unchanged when invoking LockDoorUseCase on it")
    }

    @Test
    fun lockDoor_nonexistentDevice_doesNotCrash() = runTest {
        val repo = InMemoryDeviceRepository(emptyList())
        val useCase = LockDoorUseCase(repo, FakeDeviceControlPort())

        useCase(DeviceId("nonexistent"), lock = true)

        assertTrue(repo.observeAllDevices().first().isEmpty())
    }
}

// endregion

// region BulkToggleDevicesByTypeInRoomUseCase

class BulkToggleDevicesByTypeInRoomUseCaseTest {

    @Test
    fun turnOnLightsInRoom_onlyAffectsTargetRoom() = runTest {
        val light1 = Light(DeviceId("l1"), "L1", RoomId("room-1"), isOn = false, Color.WHITE, 80)
        val light2 = Light(DeviceId("l2"), "L2", RoomId("room-2"), isOn = false, Color.WHITE, 60)
        val deviceRepo = InMemoryDeviceRepository(listOf(light1, light2))
        val roomRepo = InMemoryRoomRepository(listOf(
            Room(RoomId("room-1"), "Living Room", null, listOf(DeviceId("l1"))),
        ))
        val useCase = BulkToggleDevicesByTypeInRoomUseCase(deviceRepo, roomRepo, FakeDeviceControlPort())

        useCase(RoomId("room-1"), DeviceType.LIGHT, turnOn = true)

        val l1 = deviceRepo.observeDevice(DeviceId("l1")).first() as Light
        val l2 = deviceRepo.observeDevice(DeviceId("l2")).first() as Light
        assertTrue(l1.isOn)
        assertFalse(l2.isOn)
    }

    @Test
    fun turnOffLightsInRoom_setsOnlyRoomLightsOff() = runTest {
        val light1 = Light(DeviceId("l1"), "L1", RoomId("room-1"), isOn = true, Color.WHITE, 80)
        val light2 = Light(DeviceId("l2"), "L2", RoomId("room-1"), isOn = true, Color.WHITE, 60)
        val deviceRepo = InMemoryDeviceRepository(listOf(light1, light2))
        val roomRepo = InMemoryRoomRepository(listOf(
            Room(RoomId("room-1"), "Living Room", null, listOf(DeviceId("l1"), DeviceId("l2"))),
        ))
        val useCase = BulkToggleDevicesByTypeInRoomUseCase(deviceRepo, roomRepo, FakeDeviceControlPort())

        useCase(RoomId("room-1"), DeviceType.LIGHT, turnOn = false)

        val lights = deviceRepo.observeDevicesByType(DeviceType.LIGHT).first()
        assertTrue(lights.all { !(it as Light).isOn })
    }

    @Test
    fun bulkToggleInRoom_unknownRoom_doesNotCrash() = runTest {
        val light = Light(DeviceId("l1"), "L1", RoomId("room-1"), isOn = false, Color.WHITE, 80)
        val deviceRepo = InMemoryDeviceRepository(listOf(light))
        val roomRepo = InMemoryRoomRepository(emptyList())
        val useCase = BulkToggleDevicesByTypeInRoomUseCase(deviceRepo, roomRepo, FakeDeviceControlPort())

        useCase(RoomId("unknown"), DeviceType.LIGHT, turnOn = true)

        val result = deviceRepo.observeDevice(DeviceId("l1")).first() as Light
        assertFalse(result.isOn, "Light should remain off when room is not found")
    }
}

// endregion

// region RoomRepository (save / delete)

class RoomRepositoryTest {

    @Test
    fun saveRoom_addsNewRoom() = runTest {
        val repo = InMemoryRoomRepository(emptyList())

        val room = Room(RoomId("room-1"), "Living Room", null, listOf(DeviceId("d-1")))
        repo.save(room)

        val result = repo.observeRoom(RoomId("room-1")).first()
        assertNotNull(result)
        assertEquals("Living Room", result.name)
        assertEquals(1, result.deviceIds.size)
    }

    @Test
    fun saveRoom_updatesExistingRoom() = runTest {
        val room = Room(RoomId("room-1"), "Living Room", null, listOf(DeviceId("d-1")))
        val repo = InMemoryRoomRepository(listOf(room))

        val updated = room.copy(name = "Salon", deviceIds = listOf(DeviceId("d-1"), DeviceId("d-2")))
        repo.save(updated)

        val result = repo.observeRoom(RoomId("room-1")).first()
        assertNotNull(result)
        assertEquals("Salon", result.name)
        assertEquals(2, result.deviceIds.size)
    }

    @Test
    fun deleteRoom_removesRoom() = runTest {
        val room = Room(RoomId("room-1"), "Living Room", null, emptyList())
        val repo = InMemoryRoomRepository(listOf(room))

        repo.delete(RoomId("room-1"))

        val result = repo.observeRoom(RoomId("room-1")).first()
        assertNull(result)
        assertTrue(repo.observeAllRooms().first().isEmpty())
    }

    @Test
    fun deleteNonexistentRoom_doesNotCrash() = runTest {
        val repo = InMemoryRoomRepository(emptyList())

        repo.delete(RoomId("nonexistent"))

        assertTrue(repo.observeAllRooms().first().isEmpty())
    }
}

// endregion
