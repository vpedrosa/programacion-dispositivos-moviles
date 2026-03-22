package com.vpedrosa.smarthome.wear.ui.device_control

import com.vpedrosa.smarthome.wear.device_control.domain.ActionResult
import com.vpedrosa.smarthome.wear.device_control.domain.DeviceCommandPort
import com.vpedrosa.smarthome.wear.device_control.domain.DeviceListResult
import com.vpedrosa.smarthome.wear.device_control.domain.model.WearDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceControlViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePort: FakeDeviceCommandPort
    private lateinit var viewModel: DeviceControlViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePort = FakeDeviceCommandPort()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads devices on init and groups by room`() = runTest {
        fakePort.deviceListResult = DeviceListResult.Success(
            listOf(
                WearDevice(id = "1", name = "Luz", type = "LIGHT", roomName = "Salón", isOn = true),
                WearDevice(id = "2", name = "TV", type = "SMART_TV", roomName = "Salón"),
                WearDevice(id = "3", name = "Lock", type = "LOCK", roomName = "Entrada"),
            )
        )
        viewModel = DeviceControlViewModel(fakePort)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.devicesByRoom.size)
        assertEquals(2, state.devicesByRoom["Salón"]?.size)
        assertEquals(1, state.devicesByRoom["Entrada"]?.size)
    }

    @Test
    fun `toggleDevice updates device in list`() = runTest {
        val light = WearDevice(id = "1", name = "Luz", type = "LIGHT", roomName = "Salón", isOn = false)
        fakePort.deviceListResult = DeviceListResult.Success(listOf(light))
        viewModel = DeviceControlViewModel(fakePort)
        advanceUntilIdle()

        fakePort.actionResult = ActionResult.Success(light.copy(isOn = true))
        viewModel.toggleDevice("1")
        advanceUntilIdle()

        val updated = viewModel.uiState.value.devicesByRoom["Salón"]?.first()
        assertTrue(updated!!.isOn)
    }

    @Test
    fun `toggleDevice shows error on failure`() = runTest {
        val light = WearDevice(id = "1", name = "Luz", type = "LIGHT", roomName = "Salón")
        fakePort.deviceListResult = DeviceListResult.Success(listOf(light))
        viewModel = DeviceControlViewModel(fakePort)
        advanceUntilIdle()

        fakePort.actionResult = ActionResult.Error("Timeout")
        viewModel.toggleDevice("1")
        advanceUntilIdle()

        assertEquals("Timeout", viewModel.uiState.value.error)
    }

    @Test
    fun `devices without room go to unassigned group`() = runTest {
        fakePort.deviceListResult = DeviceListResult.Success(
            listOf(WearDevice(id = "1", name = "Luz", type = "LIGHT", roomName = null))
        )
        viewModel = DeviceControlViewModel(fakePort)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.devicesByRoom[DeviceControlViewModel.UNASSIGNED_ROOM]?.size)
        assertNull(state.devicesByRoom["Salón"])
    }

    // -- Test fake --

    private class FakeDeviceCommandPort : DeviceCommandPort {
        var deviceListResult: DeviceListResult = DeviceListResult.Success(emptyList())
        var actionResult: ActionResult = ActionResult.Success(
            WearDevice(id = "", name = "", type = "", roomName = null)
        )

        override suspend fun requestDeviceList() = deviceListResult
        override suspend fun sendToggleAction(deviceId: String) = actionResult
    }
}
