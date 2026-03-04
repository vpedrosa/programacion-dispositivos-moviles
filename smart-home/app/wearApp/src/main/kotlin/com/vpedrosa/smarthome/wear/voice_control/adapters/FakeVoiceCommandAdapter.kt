package com.vpedrosa.smarthome.wear.voice_control.adapters

import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandPort
import com.vpedrosa.smarthome.wear.voice_control.domain.ports.VoiceCommandResult
import kotlinx.coroutines.delay

/**
 * Fake driven adapter that simulates communication with the main app.
 * In production, this would be replaced by a WearableDataLayerAdapter
 * that uses Google's Wearable Data Layer API (MessageClient).
 */
class FakeVoiceCommandAdapter : VoiceCommandPort {

    private val knownCommands = mapOf(
        "enciende" to "Dispositivo encendido",
        "apaga" to "Dispositivo apagado",
        "sube" to "Intensidad aumentada",
        "baja" to "Intensidad reducida",
        "temperatura" to "Temperatura actual: 22 C",
        "luces" to "Luces del salon encendidas",
        "luz" to "Luz encendida",
        "cerradura" to "Cerradura activada",
        "alarma" to "Alarma activada",
        "ventilador" to "Ventilador encendido",
        "persiana" to "Persiana abierta",
    )

    override suspend fun sendCommand(transcribedText: String): VoiceCommandResult {
        // Simulate network round-trip to the phone app
        delay(1_200L)

        val lowerText = transcribedText.lowercase()

        val matchedResponse = knownCommands.entries
            .firstOrNull { (keyword, _) -> lowerText.contains(keyword) }
            ?.value

        return if (matchedResponse != null) {
            VoiceCommandResult.Success(matchedResponse)
        } else {
            VoiceCommandResult.Error("Comando no reconocido: \"$transcribedText\"")
        }
    }
}
