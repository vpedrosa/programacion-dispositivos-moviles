package com.vpedrosa.smarthome.wear.voice_control

import com.vpedrosa.smarthome.wear.voice_control.adapters.FakeWearSpeechRecognizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FakeWearSpeechRecognizerTest {

    @Test
    fun `starts in non-listening state`() = runTest {
        val recognizer = FakeWearSpeechRecognizer(backgroundScope)
        val listening = recognizer.isListening.first()
        assertFalse(listening)
    }

    @Test
    fun `emits recognized text after delay`() = runTest(UnconfinedTestDispatcher()) {
        val recognizer = FakeWearSpeechRecognizer(backgroundScope)

        var capturedText: String? = null
        val job = launch {
            recognizer.recognizedText.first { text ->
                capturedText = text
                true
            }
        }

        recognizer.startListening()
        assertTrue(recognizer.isListening.first())

        // Wait for the simulated delay
        testScheduler.advanceTimeBy(2_100L)

        job.join()
        assertEquals("Enciende las luces del salón", capturedText)
    }

    @Test
    fun `stopListening cancels pending recognition`() = runTest {
        val recognizer = FakeWearSpeechRecognizer(backgroundScope)

        recognizer.startListening()
        recognizer.stopListening()

        assertFalse(recognizer.isListening.first())
    }

    @Test
    fun `cycles through sample commands`() = runTest(UnconfinedTestDispatcher()) {
        val recognizer = FakeWearSpeechRecognizer(backgroundScope)
        val texts = mutableListOf<String>()

        // Collect first two commands
        repeat(2) {
            val job = launch {
                recognizer.recognizedText.first { text ->
                    texts.add(text)
                    true
                }
            }
            recognizer.startListening()
            testScheduler.advanceTimeBy(2_100L)
            job.join()
        }

        assertEquals(2, texts.size)
        // First and second commands should be different
        assertEquals("Enciende las luces del salón", texts[0])
        assertEquals("Apaga la luz de la cocina", texts[1])
    }
}
