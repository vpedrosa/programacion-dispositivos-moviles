package com.vpedrosa.smarthome.voice

import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.voice.domain.model.ParsedVoiceCommand
import com.vpedrosa.smarthome.voice.application.ParseVoiceCommandUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParseVoiceCommandUseCaseTest {

    private val parse = ParseVoiceCommandUseCase()

    // -- Spanish: toggle lights --

    @Test
    fun parseSpanishTurnOnLights() {
        val result = parse("Enciende las luces del salón")
        assertIs<ParsedVoiceCommand.ToggleDevices>(result)
        assertEquals(DeviceType.LIGHT, result.deviceType)
        assertTrue(result.turnOn)
        assertEquals("salon", result.roomName)
    }

    @Test
    fun parseSpanishTurnOffLights() {
        val result = parse("Apaga las luces")
        assertIs<ParsedVoiceCommand.ToggleDevices>(result)
        assertEquals(DeviceType.LIGHT, result.deviceType)
        assertTrue(!result.turnOn)
        assertNull(result.roomName)
    }

    // -- Spanish: TV --

    @Test
    fun parseSpanishTurnOnTv() {
        val result = parse("Enciende la tele")
        assertIs<ParsedVoiceCommand.ToggleDevices>(result)
        assertEquals(DeviceType.SMART_TV, result.deviceType)
        assertTrue(result.turnOn)
    }

    @Test
    fun parseSpanishTurnOffTv() {
        val result = parse("Apaga la televisión")
        assertIs<ParsedVoiceCommand.ToggleDevices>(result)
        assertEquals(DeviceType.SMART_TV, result.deviceType)
        assertTrue(!result.turnOn)
    }

    // -- Spanish: switches --

    @Test
    fun parseSpanishTurnOnSwitches() {
        val result = parse("Enciende los interruptores")
        assertIs<ParsedVoiceCommand.ToggleDevices>(result)
        assertEquals(DeviceType.SWITCH, result.deviceType)
        assertTrue(result.turnOn)
    }

    // -- Spanish: blinds --

    @Test
    fun parseSpanishOpenBlinds() {
        val result = parse("Abre las persianas del dormitorio")
        assertIs<ParsedVoiceCommand.SetBlinds>(result)
        assertTrue(result.open)
        assertEquals("dormitorio", result.roomName)
    }

    @Test
    fun parseSpanishCloseBlinds() {
        val result = parse("Cierra las persianas")
        assertIs<ParsedVoiceCommand.SetBlinds>(result)
        assertTrue(!result.open)
    }

    // -- Spanish: lock --

    @Test
    fun parseSpanishLockDoor() {
        val result = parse("Cierra la puerta de entrada")
        assertIs<ParsedVoiceCommand.ToggleLock>(result)
        assertTrue(result.lock)
        assertEquals("entrada", result.doorName)
    }

    @Test
    fun parseSpanishUnlockDoor() {
        val result = parse("Abre la puerta del garaje")
        assertIs<ParsedVoiceCommand.ToggleLock>(result)
        assertTrue(!result.lock)
        assertEquals("garaje", result.doorName)
    }

    // -- Spanish: thermostat --

    @Test
    fun parseSpanishSetTemperature() {
        val result = parse("Pon la temperatura a 22 grados")
        assertIs<ParsedVoiceCommand.SetThermostat>(result)
        assertEquals(22.0, result.targetTemperature)
    }

    @Test
    fun parseSpanishSetTemperatureWithDecimals() {
        val result = parse("Sube la temperatura a 23,5 grados")
        assertIs<ParsedVoiceCommand.SetThermostat>(result)
        assertEquals(23.5, result.targetTemperature)
    }

    // -- English: toggle lights --

    @Test
    fun parseEnglishTurnOnLights() {
        val result = parse("Turn on the lights in the kitchen")
        assertIs<ParsedVoiceCommand.ToggleDevices>(result)
        assertEquals(DeviceType.LIGHT, result.deviceType)
        assertTrue(result.turnOn)
        assertEquals("kitchen", result.roomName)
    }

    @Test
    fun parseEnglishTurnOffLights() {
        val result = parse("Turn off the lights")
        assertIs<ParsedVoiceCommand.ToggleDevices>(result)
        assertEquals(DeviceType.LIGHT, result.deviceType)
        assertTrue(!result.turnOn)
    }

    // -- English: blinds --

    @Test
    fun parseEnglishOpenBlinds() {
        val result = parse("Open the blinds")
        assertIs<ParsedVoiceCommand.SetBlinds>(result)
        assertTrue(result.open)
    }

    @Test
    fun parseEnglishCloseBlinds() {
        val result = parse("Close the blinds in the bedroom")
        assertIs<ParsedVoiceCommand.SetBlinds>(result)
        assertTrue(!result.open)
        assertEquals("bedroom", result.roomName)
    }

    // -- English: thermostat --

    @Test
    fun parseEnglishSetTemperature() {
        val result = parse("Set temperature to 24 degrees")
        assertIs<ParsedVoiceCommand.SetThermostat>(result)
        assertEquals(24.0, result.targetTemperature)
    }

    // -- English: lock --

    @Test
    fun parseEnglishLockDoor() {
        val result = parse("Lock the door")
        assertIs<ParsedVoiceCommand.ToggleLock>(result)
        assertTrue(result.lock)
    }

    @Test
    fun parseEnglishUnlockDoor() {
        val result = parse("Unlock the door")
        assertIs<ParsedVoiceCommand.ToggleLock>(result)
        assertTrue(!result.lock)
    }

    // -- Unknown --

    @Test
    fun parseUnknownCommand() {
        val result = parse("Haz algo raro")
        assertIs<ParsedVoiceCommand.Unknown>(result)
    }

    @Test
    fun parseEmptyString() {
        val result = parse("")
        assertIs<ParsedVoiceCommand.Unknown>(result)
    }
}
