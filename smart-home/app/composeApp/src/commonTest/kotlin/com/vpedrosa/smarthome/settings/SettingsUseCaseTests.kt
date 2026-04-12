package com.vpedrosa.smarthome.settings

import com.vpedrosa.smarthome.settings.application.ToggleNotificationsUseCase
import com.vpedrosa.smarthome.settings.infrastructure.InMemoryAppSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsUseCaseTests {

    private val repo = InMemoryAppSettingsRepository()
    private val toggleNotifications = ToggleNotificationsUseCase(repo)

    @Test
    fun toggleNotifications_disablesWhenEnabled() = runTest {
        // Default state has notifications enabled
        assertTrue(repo.observeSettings().first().notificationsEnabled)

        toggleNotifications()

        assertFalse(repo.observeSettings().first().notificationsEnabled)
    }

    @Test
    fun toggleNotifications_enablesWhenDisabled() = runTest {
        toggleNotifications() // disable first
        assertFalse(repo.observeSettings().first().notificationsEnabled)

        toggleNotifications() // re-enable

        assertTrue(repo.observeSettings().first().notificationsEnabled)
    }
}
