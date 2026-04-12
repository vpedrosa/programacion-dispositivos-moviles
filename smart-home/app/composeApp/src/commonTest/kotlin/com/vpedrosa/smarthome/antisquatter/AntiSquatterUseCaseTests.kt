package com.vpedrosa.smarthome.antisquatter

import com.vpedrosa.smarthome.antisquatter.application.ToggleAntiSquatterUseCase
import com.vpedrosa.smarthome.antisquatter.application.UpdateAntiSquatterActionDurationUseCase
import com.vpedrosa.smarthome.antisquatter.application.UpdateAntiSquatterEndTimeUseCase
import com.vpedrosa.smarthome.antisquatter.application.UpdateAntiSquatterStartTimeUseCase
import com.vpedrosa.smarthome.antisquatter.domain.model.AntiSquatterConfig
import com.vpedrosa.smarthome.antisquatter.infrastructure.InMemoryAntiSquatterRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AntiSquatterUseCaseTests {

    private val repo = InMemoryAntiSquatterRepository()

    // -- ToggleAntiSquatterUseCase --

    @Test
    fun toggleAntiSquatter_enablesWhenDisabled() = runTest {
        val toggle = ToggleAntiSquatterUseCase(repo)

        toggle()

        assertTrue(repo.observeConfig().first().isEnabled)
    }

    @Test
    fun toggleAntiSquatter_disablesWhenEnabled() = runTest {
        repo.saveConfig(AntiSquatterConfig.DEFAULT.copy(isEnabled = true))
        val toggle = ToggleAntiSquatterUseCase(repo)

        toggle()

        assertFalse(repo.observeConfig().first().isEnabled)
    }

    @Test
    fun toggleAntiSquatter_doesNotAffectOtherFields() = runTest {
        val config = AntiSquatterConfig.DEFAULT.copy(startHour = 20, actionDurationMinutes = 15)
        repo.saveConfig(config)
        val toggle = ToggleAntiSquatterUseCase(repo)

        toggle()

        val result = repo.observeConfig().first()
        assertEquals(20, result.startHour)
        assertEquals(15, result.actionDurationMinutes)
    }

    // -- UpdateAntiSquatterStartTimeUseCase --

    @Test
    fun updateStartTime_savesNewTime() = runTest {
        val update = UpdateAntiSquatterStartTimeUseCase(repo)

        update(hour = 8, minute = 30)

        val config = repo.observeConfig().first()
        assertEquals(8, config.startHour)
        assertEquals(30, config.startMinute)
    }

    @Test
    fun updateStartTime_doesNotAffectEndTime() = runTest {
        val update = UpdateAntiSquatterStartTimeUseCase(repo)

        update(hour = 10, minute = 0)

        val config = repo.observeConfig().first()
        assertEquals(AntiSquatterConfig.DEFAULT.endHour, config.endHour)
        assertEquals(AntiSquatterConfig.DEFAULT.endMinute, config.endMinute)
    }

    // -- UpdateAntiSquatterEndTimeUseCase --

    @Test
    fun updateEndTime_savesNewTime() = runTest {
        val update = UpdateAntiSquatterEndTimeUseCase(repo)

        update(hour = 22, minute = 45)

        val config = repo.observeConfig().first()
        assertEquals(22, config.endHour)
        assertEquals(45, config.endMinute)
    }

    @Test
    fun updateEndTime_doesNotAffectStartTime() = runTest {
        val update = UpdateAntiSquatterEndTimeUseCase(repo)

        update(hour = 23, minute = 0)

        val config = repo.observeConfig().first()
        assertEquals(AntiSquatterConfig.DEFAULT.startHour, config.startHour)
        assertEquals(AntiSquatterConfig.DEFAULT.startMinute, config.startMinute)
    }

    // -- UpdateAntiSquatterActionDurationUseCase --

    @Test
    fun updateActionDuration_savesNewDuration() = runTest {
        val update = UpdateAntiSquatterActionDurationUseCase(repo)

        update(minutes = 20)

        assertEquals(20, repo.observeConfig().first().actionDurationMinutes)
    }

    @Test
    fun updateActionDuration_zeroIsNoOp() = runTest {
        val original = repo.observeConfig().first().actionDurationMinutes
        val update = UpdateAntiSquatterActionDurationUseCase(repo)

        update(minutes = 0)

        assertEquals(original, repo.observeConfig().first().actionDurationMinutes)
    }

    @Test
    fun updateActionDuration_negativeIsNoOp() = runTest {
        val original = repo.observeConfig().first().actionDurationMinutes
        val update = UpdateAntiSquatterActionDurationUseCase(repo)

        update(minutes = -5)

        assertEquals(original, repo.observeConfig().first().actionDurationMinutes)
    }
}
