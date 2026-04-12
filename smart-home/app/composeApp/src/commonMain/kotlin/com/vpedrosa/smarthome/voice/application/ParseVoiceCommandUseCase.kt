package com.vpedrosa.smarthome.voice.application

import com.vpedrosa.smarthome.device.domain.model.DeviceType
import com.vpedrosa.smarthome.voice.domain.model.ParsedVoiceCommand

/**
 * Parses a text string (from speech-to-text) into a [ParsedVoiceCommand].
 * Supports Spanish and English commands using simple keyword matching.
 * No external dependencies -- pure domain logic.
 */
class ParseVoiceCommandUseCase {

    operator fun invoke(text: String): ParsedVoiceCommand {
        val normalized = text.trim().lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")

        return parseThermostat(normalized)
            ?: parseBlinds(normalized)
            ?: parseLock(normalized)
            ?: parseToggle(normalized)
            ?: ParsedVoiceCommand.Unknown
    }

    // -- Thermostat: "sube/baja la temperatura a X grados" / "set temperature to X degrees" --

    private fun parseThermostat(text: String): ParsedVoiceCommand.SetThermostat? {
        val esPattern = Regex("""(?:sube|baja|pon|ajusta)\s+(?:la\s+)?temperatura\s+(?:a\s+)?(\d+(?:[.,]\d+)?)\s*(?:grados?)?""")
        val enPattern = Regex("""(?:set|change|adjust)\s+(?:the\s+)?temperature\s+(?:to\s+)?(\d+(?:\.\d+)?)\s*(?:degrees?)?""")

        val esMatch = esPattern.find(text)
        if (esMatch != null) {
            val temp = esMatch.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return null
            val room = extractRoomNameEs(text)
            return ParsedVoiceCommand.SetThermostat(temp, room)
        }

        val enMatch = enPattern.find(text)
        if (enMatch != null) {
            val temp = enMatch.groupValues[1].toDoubleOrNull() ?: return null
            val room = extractRoomNameEn(text)
            return ParsedVoiceCommand.SetThermostat(temp, room)
        }

        return null
    }

    // -- Blinds: "abre/cierra las persianas" / "open/close the blinds" --

    private fun parseBlinds(text: String): ParsedVoiceCommand.SetBlinds? {
        val esOpenPattern = Regex("""abre?\s+(?:las?\s+)?persianas?""")
        val esClosePattern = Regex("""cierra?\s+(?:las?\s+)?persianas?""")
        val enOpenPattern = Regex("""open\s+(?:the\s+)?blinds?""")
        val enClosePattern = Regex("""close\s+(?:the\s+)?blinds?""")

        return when {
            esOpenPattern.containsMatchIn(text) ->
                ParsedVoiceCommand.SetBlinds(open = true, roomName = extractRoomNameEs(text))
            esClosePattern.containsMatchIn(text) ->
                ParsedVoiceCommand.SetBlinds(open = false, roomName = extractRoomNameEs(text))
            enOpenPattern.containsMatchIn(text) ->
                ParsedVoiceCommand.SetBlinds(open = true, roomName = extractRoomNameEn(text))
            enClosePattern.containsMatchIn(text) ->
                ParsedVoiceCommand.SetBlinds(open = false, roomName = extractRoomNameEn(text))
            else -> null
        }
    }

    // -- Lock: "abre/cierra la puerta" / "lock/unlock the door" --

    private fun parseLock(text: String): ParsedVoiceCommand.ToggleLock? {
        val esLockPattern = Regex("""cierra?\s+(?:la\s+)?(?:puerta|cerradura)""")
        val esUnlockPattern = Regex("""abre?\s+(?:la\s+)?(?:puerta|cerradura)""")
        val enLockPattern = Regex("""\block\s+(?:the\s+)?(?:door|lock)""")
        val enUnlockPattern = Regex("""\bunlock\s+(?:the\s+)?(?:door|lock)""")

        // Check unlock before lock (unlock contains "lock" as substring)
        return when {
            esUnlockPattern.containsMatchIn(text) -> {
                val doorName = extractDoorNameEs(text)
                ParsedVoiceCommand.ToggleLock(lock = false, doorName = doorName)
            }
            esLockPattern.containsMatchIn(text) -> {
                val doorName = extractDoorNameEs(text)
                ParsedVoiceCommand.ToggleLock(lock = true, doorName = doorName)
            }
            enUnlockPattern.containsMatchIn(text) -> {
                val doorName = extractDoorNameEn(text)
                ParsedVoiceCommand.ToggleLock(lock = false, doorName = doorName)
            }
            enLockPattern.containsMatchIn(text) -> {
                val doorName = extractDoorNameEn(text)
                ParsedVoiceCommand.ToggleLock(lock = true, doorName = doorName)
            }
            else -> null
        }
    }

    // -- Toggle devices: lights, TV, switches --

    private fun parseToggle(text: String): ParsedVoiceCommand.ToggleDevices? {
        // Spanish turn-on keywords
        val esOnKeywords = listOf("enciende", "encender", "activa", "activar", "prende", "prender")
        val esOffKeywords = listOf("apaga", "apagar", "desactiva", "desactivar")

        // English turn-on keywords
        val enOnKeywords = listOf("turn on", "switch on", "enable")
        val enOffKeywords = listOf("turn off", "switch off", "disable")

        val turnOn: Boolean?
        val isSpanish: Boolean

        when {
            esOnKeywords.any { text.contains(it) } -> { turnOn = true; isSpanish = true }
            esOffKeywords.any { text.contains(it) } -> { turnOn = false; isSpanish = true }
            enOnKeywords.any { text.contains(it) } -> { turnOn = true; isSpanish = false }
            enOffKeywords.any { text.contains(it) } -> { turnOn = false; isSpanish = false }
            else -> return null
        }

        val deviceType = resolveDeviceType(text, isSpanish) ?: return null
        val roomName = if (isSpanish) extractRoomNameEs(text) else extractRoomNameEn(text)

        return ParsedVoiceCommand.ToggleDevices(deviceType, turnOn, roomName)
    }

    private fun resolveDeviceType(text: String, isSpanish: Boolean): DeviceType? {
        return if (isSpanish) {
            when {
                text.contains("luz") || text.contains("luces") || text.contains("bombilla") -> DeviceType.LIGHT
                text.contains("tele") || text.contains("television") || text.contains("tv") -> DeviceType.SMART_TV
                text.contains("interruptor") || text.contains("interruptores") -> DeviceType.SWITCH
                text.contains("termostato") || text.contains("calefaccion") -> DeviceType.THERMOSTAT
                else -> null
            }
        } else {
            when {
                text.contains("light") || text.contains("lights") || text.contains("bulb") -> DeviceType.LIGHT
                text.contains("tv") || text.contains("television") || text.contains("telly") -> DeviceType.SMART_TV
                text.contains("switch") || text.contains("switches") -> DeviceType.SWITCH
                text.contains("thermostat") || text.contains("heating") -> DeviceType.THERMOSTAT
                else -> null
            }
        }
    }

    // -- Room name extraction --

    private fun extractRoomNameEs(text: String): String? {
        // Matches "del salón", "de la cocina", "de el salón", "en el salón", etc.
        // Word boundary prevents matching "de" inside words like "enciende"
        val pattern = Regex("""\b(?:de\s+las|de\s+los|de\s+la|de\s+el|del|en\s+las|en\s+los|en\s+la|en\s+el)\s+(.+?)[\s.,;!?]*$""")
        val match = pattern.find(text) ?: return null
        return match.groupValues[1].trim().ifBlank { null }
    }

    private fun extractRoomNameEn(text: String): String? {
        // Matches "in the living room", "in the kitchen"
        val pattern = Regex("""(?:in the|in|of the|of)\s+(.+?)[\s.,;!?]*$""")
        val match = pattern.find(text) ?: return null
        return match.groupValues[1].trim().ifBlank { null }
    }

    private fun extractDoorNameEs(text: String): String? {
        // "puerta de entrada", "puerta del garaje"
        val pattern = Regex("""puerta\s+(?:de(?:l)?\s+)?(.+?)[\s.,;!?]*$""")
        val match = pattern.find(text) ?: return null
        return match.groupValues[1].trim().ifBlank { null }
    }

    private fun extractDoorNameEn(text: String): String? {
        val pattern = Regex("""(?:door|lock)\s+(?:of the|of|in the|in)?\s*(.+?)[\s.,;!?]*$""")
        val match = pattern.find(text) ?: return null
        return match.groupValues[1].trim().ifBlank { null }
    }
}
