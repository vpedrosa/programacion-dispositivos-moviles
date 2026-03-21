package com.vpedrosa.smarthome.wear.voice_control

import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandPort
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceControlViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCommandPort: FakeVoiceCommandPortForTest
    private lateinit var viewModel: VoiceControlViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCommandPort = FakeVoiceCommandPortForTest()
        viewModel = VoiceControlViewModel(fakeCommandPort)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(VoiceStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `onSpeechResult transitions to Processing then Result`() = runTest {
        fakeCommandPort.nextResult = VoiceCommandResult.Success("Luces encendidas")

        viewModel.onSpeechResult("Enciende las luces")
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertEquals(VoiceStatus.Result, state.status)
        assertEquals("Enciende las luces", state.transcribedText)
        assertEquals("Luces encendidas", state.resultMessage)
    }

    @Test
    fun `onSpeechResult transitions to Error on command failure`() = runTest {
        fakeCommandPort.nextResult = VoiceCommandResult.Error("Comando no reconocido")

        viewModel.onSpeechResult("foo bar baz")
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertEquals(VoiceStatus.Error, state.status)
        assertEquals("Comando no reconocido", state.resultMessage)
    }

    @Test
    fun `onSpeechError shows error state`() = runTest {
        viewModel.onSpeechError("No se detectó voz")
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertEquals(VoiceStatus.Error, state.status)
        assertEquals("No se detectó voz", state.resultMessage)
    }

    @Test
    fun `auto-resets to Idle after result is shown`() = runTest {
        fakeCommandPort.nextResult = VoiceCommandResult.Success("Luces encendidas")

        viewModel.onSpeechResult("Enciende las luces")
        advanceTimeBy(100)
        assertEquals(VoiceStatus.Result, viewModel.uiState.value.status)

        // Advance past the 4-second auto-reset delay
        advanceTimeBy(4_100)
        assertEquals(VoiceStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `auto-resets to Idle after error is shown`() = runTest {
        viewModel.onSpeechError("Error de audio")
        advanceTimeBy(100)
        assertEquals(VoiceStatus.Error, viewModel.uiState.value.status)

        advanceTimeBy(4_100)
        assertEquals(VoiceStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `ignores blank speech result`() = runTest {
        viewModel.onSpeechResult("   ")
        advanceUntilIdle()

        assertEquals(VoiceStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `handles exception from voice command port`() = runTest {
        fakeCommandPort.throwOnSend = RuntimeException("Connection lost")

        viewModel.onSpeechResult("Enciende las luces")
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertEquals(VoiceStatus.Error, state.status)
        assertEquals("Connection lost", state.resultMessage)
    }

    // -- Test fake --

    private class FakeVoiceCommandPortForTest : VoiceCommandPort {
        var nextResult: VoiceCommandResult = VoiceCommandResult.Success("OK")
        var throwOnSend: Exception? = null
        var suspendUntilCompleted = false
        private var deferred = CompletableDeferred<Unit>()

        override suspend fun sendCommand(transcribedText: String): VoiceCommandResult {
            throwOnSend?.let { throw it }
            if (suspendUntilCompleted) {
                deferred.await()
            }
            return nextResult
        }

        fun complete() {
            deferred.complete(Unit)
        }
    }
}
