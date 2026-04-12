package com.vpedrosa.smarthome.voice

import com.vpedrosa.smarthome.voice.application.RecordVoiceCommandUseCase
import com.vpedrosa.smarthome.voice.domain.model.VoiceCommandResult
import com.vpedrosa.smarthome.voice.infrastructure.persistence.InMemoryVoiceCommandRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordVoiceCommandUseCaseTest {

    private val repo = InMemoryVoiceCommandRepository()
    private val record = RecordVoiceCommandUseCase(repo)

    private val successResult = VoiceCommandResult(success = true, message = "Done", devicesAffected = 1)
    private val failResult = VoiceCommandResult(success = false, message = "Not found", devicesAffected = 0)

    @Test
    fun record_addsCommandToRepository() = runTest {
        record("enciende las luces", successResult)

        val commands = repo.observeRecentCommands().first()
        assertEquals(1, commands.size)
        assertEquals("enciende las luces", commands.first().text)
        assertEquals(successResult, commands.first().result)
    }

    @Test
    fun record_mostRecentCommandIsFirst() = runTest {
        record("primera orden", successResult)
        record("segunda orden", failResult)

        val commands = repo.observeRecentCommands().first()
        assertEquals("segunda orden", commands.first().text)
        assertEquals("primera orden", commands[1].text)
    }

    @Test
    fun record_capsAt10Commands() = runTest {
        repeat(11) { i ->
            record("orden $i", successResult)
        }

        val commands = repo.observeRecentCommands().first()
        assertEquals(10, commands.size)
        assertEquals("orden 10", commands.first().text)
    }

    @Test
    fun record_storesTimestamp() = runTest {
        record("apaga el televisor", successResult)

        val command = repo.observeRecentCommands().first().first()
        assertTrue(command.timestamp.epochSeconds > 0)
    }
}
