package com.vpedrosa.smarthome.device.domain.ports

import kotlinx.coroutines.flow.Flow

/**
 * Driven port for speech-to-text recognition.
 * Platform adapters (Android SpeechRecognizer, iOS Speech framework)
 * implement this interface.
 */
interface SpeechRecognizerPort {

    /**
     * Observable state of whether the recognizer is currently listening.
     */
    val isListening: Flow<Boolean>

    /**
     * Starts listening for speech input.
     */
    fun startListening()

    /**
     * Stops listening.
     */
    fun stopListening()

    /**
     * Flow of partial or final recognized text results.
     */
    val recognizedText: Flow<String>

    /**
     * Flow of errors that occur during recognition.
     */
    val errors: Flow<String>
}
