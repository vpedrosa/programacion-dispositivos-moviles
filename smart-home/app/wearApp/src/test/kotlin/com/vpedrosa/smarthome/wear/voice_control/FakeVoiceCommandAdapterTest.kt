package com.vpedrosa.smarthome.wear.voice_control

import com.vpedrosa.smarthome.wear.voice_control.adapters.FakeVoiceCommandAdapter
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FakeVoiceCommandAdapterTest {

    private val adapter = FakeVoiceCommandAdapter()

    @Test
    fun `returns success when command contains known keyword`() = runTest {
        val result = adapter.sendCommand("Enciende las luces")
        assertIs<VoiceCommandResult.Success>(result)
        assertEquals("Dispositivo encendido", result.responseMessage)
    }

    @Test
    fun `returns success for temperature keyword`() = runTest {
        val result = adapter.sendCommand("Dame la temperatura")
        assertIs<VoiceCommandResult.Success>(result)
        assertEquals("Temperatura actual: 22 C", result.responseMessage)
    }

    @Test
    fun `returns error for unknown command`() = runTest {
        val result = adapter.sendCommand("reproduce musica")
        assertIs<VoiceCommandResult.Error>(result)
        assertEquals("Comando no reconocido: \"reproduce musica\"", result.errorMessage)
    }

    @Test
    fun `matching is case insensitive`() = runTest {
        val result = adapter.sendCommand("APAGA todo")
        assertIs<VoiceCommandResult.Success>(result)
        assertEquals("Dispositivo apagado", result.responseMessage)
    }
}
