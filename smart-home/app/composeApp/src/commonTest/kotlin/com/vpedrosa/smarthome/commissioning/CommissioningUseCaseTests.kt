package com.vpedrosa.smarthome.commissioning

import com.vpedrosa.smarthome.commissioning.application.ClearSimulatorHostUseCase
import com.vpedrosa.smarthome.commissioning.application.FindDiscoveredDeviceByQrUseCase
import com.vpedrosa.smarthome.commissioning.application.SearchSimulatorUseCase
import com.vpedrosa.smarthome.commissioning.domain.SimulatorDiscoveryPort
import com.vpedrosa.smarthome.commissioning.domain.model.DiscoveredDevice
import com.vpedrosa.smarthome.commissioning.infrastructure.persistence.InMemorySimulatorHostRepository
import com.vpedrosa.smarthome.device.domain.model.DeviceType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeSimulatorDiscoveryPort(private val host: String?) : SimulatorDiscoveryPort {
    override suspend fun discoverSimulatorHost(): String? = host
}

class CommissioningUseCaseTests {

    private val hostRepo = InMemorySimulatorHostRepository()

    // -- SearchSimulatorUseCase --

    @Test
    fun searchSimulator_returnsTrueAndSavesHostWhenFound() = runTest {
        val search = SearchSimulatorUseCase(FakeSimulatorDiscoveryPort("192.168.1.100"), hostRepo)

        val found = search()

        assertTrue(found)
        assertEquals("192.168.1.100", hostRepo.observeHost().first())
    }

    @Test
    fun searchSimulator_returnsFalseWhenNotFound() = runTest {
        val search = SearchSimulatorUseCase(FakeSimulatorDiscoveryPort(null), hostRepo)

        val found = search()

        assertFalse(found)
        assertNull(hostRepo.observeHost().first())
    }

    @Test
    fun searchSimulator_overwritesPreviousHost() = runTest {
        hostRepo.saveHost("10.0.0.1")
        val search = SearchSimulatorUseCase(FakeSimulatorDiscoveryPort("10.0.0.2"), hostRepo)

        search()

        assertEquals("10.0.0.2", hostRepo.observeHost().first())
    }

    // -- ClearSimulatorHostUseCase --

    @Test
    fun clearSimulatorHost_removesExistingHost() = runTest {
        hostRepo.saveHost("192.168.1.50")
        val clear = ClearSimulatorHostUseCase(hostRepo)

        clear()

        assertNull(hostRepo.observeHost().first())
    }

    @Test
    fun clearSimulatorHost_isIdempotentWhenAlreadyEmpty() = runTest {
        val clear = ClearSimulatorHostUseCase(hostRepo)

        clear()

        assertNull(hostRepo.observeHost().first())
    }

    // -- FindDiscoveredDeviceByQrUseCase --

    private val device1 = DiscoveredDevice(
        name = "Light 1",
        type = DeviceType.LIGHT,
        host = "192.168.1.10",
        port = 5540,
        discriminator = 1234,
        passcode = 20202021L,
        serialNumber = "SN001",
    )

    private val device2 = DiscoveredDevice(
        name = "Thermostat",
        type = DeviceType.THERMOSTAT,
        host = "192.168.1.11",
        port = 5540,
        discriminator = 5678,
        passcode = 11111111L,
        serialNumber = "SN002",
    )

    @Test
    fun findByQr_returnsMatchingDevice() {
        val find = FindDiscoveredDeviceByQrUseCase()

        val result = find(listOf(device1, device2), discriminator = 1234, passcode = 20202021L)

        assertNotNull(result)
        assertEquals(device1, result)
    }

    @Test
    fun findByQr_returnsNullWhenDiscriminatorDoesNotMatch() {
        val find = FindDiscoveredDeviceByQrUseCase()

        val result = find(listOf(device1, device2), discriminator = 9999, passcode = 20202021L)

        assertNull(result)
    }

    @Test
    fun findByQr_returnsNullWhenPasscodeDoesNotMatch() {
        val find = FindDiscoveredDeviceByQrUseCase()

        val result = find(listOf(device1, device2), discriminator = 1234, passcode = 99999999L)

        assertNull(result)
    }

    @Test
    fun findByQr_returnsNullForEmptyList() {
        val find = FindDiscoveredDeviceByQrUseCase()

        val result = find(emptyList(), discriminator = 1234, passcode = 20202021L)

        assertNull(result)
    }
}
