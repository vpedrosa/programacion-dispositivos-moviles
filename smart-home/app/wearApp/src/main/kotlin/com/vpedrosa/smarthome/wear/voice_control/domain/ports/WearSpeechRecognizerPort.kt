package com.vpedrosa.smarthome.wear.voice_control.domain.ports

import kotlinx.coroutines.flow.Flow

/**
 * Driven port for on-watch speech-to-text recognition.
 * The real adapter uses Android's SpeechRecognizer API.
 */
interface WearSpeechRecognizerPort {

    /**
     * Observable state of whether the recognizer is currently listening.
     */
    val isListening: Flow<Boolean>

    /**
     * Flow emitting final recognized text results.
     */
    val recognizedText: Flow<String>

    /**
     * Flow of errors that occur during recognition.
     */
    val errors: Flow<String>

    /**
     * Starts listening for speech input.
     */
    fun startListening()

    /**
     * Stops listening.
     */
    fun stopListening()

    /**
     * Releases all resources. Called when the ViewModel is cleared.
     */
    fun destroy()
}
