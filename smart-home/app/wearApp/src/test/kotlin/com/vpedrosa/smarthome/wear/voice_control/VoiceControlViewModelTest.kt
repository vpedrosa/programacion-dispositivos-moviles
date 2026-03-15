package com.vpedrosa.smarthome.wear.voice_control

import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandPort
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandResult
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.WearSpeechRecognizerPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private lateinit var fakeSpeechRecognizer: FakeSpeechRecognizerForTest
    private lateinit var fakeCommandPort: FakeVoiceCommandPortForTest
    private lateinit var viewModel: VoiceControlViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSpeechRecognizer = FakeSpeechRecognizerForTest()
        fakeCommandPort = FakeVoiceCommandPortForTest()
        viewModel = VoiceControlViewModel(fakeSpeechRecognizer, fakeCommandPort)
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
    fun `transitions to Listening when speech recognizer starts`() = runTest {
        fakeSpeechRecognizer.emitListening(true)
        advanceUntilIdle()

        assertEquals(VoiceStatus.Listening, viewModel.uiState.value.status)
    }

    @Test
    fun `transitions to Result on successful command`() = runTest {
        fakeCommandPort.nextResult = VoiceCommandResult.Success("Luces encendidas")

        fakeSpeechRecognizer.emitRecognizedText("Enciende las luces")
        // Advance enough to process the command but NOT past the 4s auto-reset
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertEquals(VoiceStatus.Result, state.status)
        assertEquals("Enciende las luces", state.transcribedText)
        assertEquals("Luces encendidas", state.resultMessage)
    }

    @Test
    fun `transitions to Error on command failure`() = runTest {
        fakeCommandPort.nextResult = VoiceCommandResult.Error("Comando no reconocido")

        fakeSpeechRecognizer.emitRecognizedText("foo bar baz")
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertEquals(VoiceStatus.Error, state.status)
        assertEquals("Comando no reconocido", state.resultMessage)
    }

    @Test
    fun `shows error when speech recognizer emits error`() = runTest {
        fakeSpeechRecognizer.emitError("No se detectó voz")
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertEquals(VoiceStatus.Error, state.status)
        assertEquals("No se detectó voz", state.resultMessage)
    }

    @Test
    fun `auto-resets to Idle after result is shown`() = runTest {
        fakeCommandPort.nextResult = VoiceCommandResult.Success("Luces encendidas")

        fakeSpeechRecognizer.emitRecognizedText("Enciende las luces")
        advanceTimeBy(100)
        assertEquals(VoiceStatus.Result, viewModel.uiState.value.status)

        // Advance past the 4-second auto-reset delay
        advanceTimeBy(4_100)
        assertEquals(VoiceStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `does not process mic press while processing`() = runTest {
        // Make the command port suspend until we explicitly complete it
        fakeCommandPort.suspendUntilCompleted = true
        fakeCommandPort.nextResult = VoiceCommandResult.Success("OK")

        fakeSpeechRecognizer.emitRecognizedText("test")
        advanceTimeBy(10)

        // The state should be Processing because the command port is suspended
        assertEquals(VoiceStatus.Processing, viewModel.uiState.value.status)

        // Pressing mic during Processing should be a no-op
        viewModel.onMicPressed()
        advanceTimeBy(10)

        // Should still be Processing, not Listening
        assertEquals(VoiceStatus.Processing, viewModel.uiState.value.status)

        // Complete the suspended command
        fakeCommandPort.complete()
        advanceTimeBy(100)
        assertEquals(VoiceStatus.Result, viewModel.uiState.value.status)
    }

    @Test
    fun `onMicPressed starts speech recognizer`() = runTest {
        viewModel.onMicPressed()
        advanceUntilIdle()

        // The fake recognizer sets isListening=true in startListening()
        assertEquals(VoiceStatus.Listening, viewModel.uiState.value.status)
    }

    @Test
    fun `onMicPressed stops speech recognizer when already listening`() = runTest {
        viewModel.onMicPressed() // start
        advanceUntilIdle()
        assertEquals(VoiceStatus.Listening, viewModel.uiState.value.status)

        viewModel.onMicPressed() // stop
        advanceUntilIdle()
        assertEquals(VoiceStatus.Idle, viewModel.uiState.value.status)
    }

    // -- Test fakes --

    private class FakeSpeechRecognizerForTest : WearSpeechRecognizerPort {
        private val _isListening = MutableStateFlow(false)
        override val isListening: Flow<Boolean> = _isListening

        private val _recognizedText = MutableSharedFlow<String>()
        override val recognizedText: Flow<String> = _recognizedText

        private val _errors = MutableSharedFlow<String>()
        override val errors: Flow<String> = _errors

        var destroyed = false

        suspend fun emitListening(value: Boolean) {
            _isListening.value = value
        }

        suspend fun emitRecognizedText(text: String) {
            _recognizedText.emit(text)
        }

        suspend fun emitError(error: String) {
            _errors.emit(error)
        }

        override fun startListening() {
            _isListening.value = true
        }

        override fun stopListening() {
            _isListening.value = false
        }

        override fun destroy() {
            destroyed = true
        }
    }

    private class FakeVoiceCommandPortForTest : VoiceCommandPort {
        var nextResult: VoiceCommandResult = VoiceCommandResult.Success("OK")
        var suspendUntilCompleted = false
        private var deferred = kotlinx.coroutines.CompletableDeferred<Unit>()

        override suspend fun sendCommand(transcribedText: String): VoiceCommandResult {
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
