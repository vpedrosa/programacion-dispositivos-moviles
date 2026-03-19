package com.vpedrosa.smarthome.shared

import com.vpedrosa.smarthome.shared.domain.model.DeviceType
import com.vpedrosa.smarthome.shared.domain.model.DeviceTypeRegistry
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceTypeRegistryTest {

    // region isToggleable

    @Test
    fun toggleableTypes_areRecognizedAsToggleable() {
        val expected = setOf(
            DeviceType.LIGHT,
            DeviceType.LOCK,
            DeviceType.SWITCH,
            DeviceType.SMART_TV,
            DeviceType.THERMOSTAT,
        )

        expected.forEach { type ->
            assertTrue(
                DeviceTypeRegistry.isToggleable(type),
                "$type should be toggleable",
            )
        }
    }

    @Test
    fun sensors_areNotToggleable() {
        val nonToggleable = setOf(
            DeviceType.BLIND,
            DeviceType.SMOKE_SENSOR,
            DeviceType.WATER_LEAK_SENSOR,
            DeviceType.TEMPERATURE_SENSOR,
            DeviceType.CONTACT_SENSOR,
        )

        nonToggleable.forEach { type ->
            assertFalse(
                DeviceTypeRegistry.isToggleable(type),
                "$type should NOT be toggleable",
            )
        }
    }

    // endregion

    // region supportsBulkToggle

    @Test
    fun bulkToggleTypes_areSupportedForBulkToggle() {
        val expected = setOf(
            DeviceType.LIGHT,
            DeviceType.LOCK,
            DeviceType.SWITCH,
            DeviceType.SMART_TV,
            DeviceType.THERMOSTAT,
            DeviceType.BLIND,
        )

        expected.forEach { type ->
            assertTrue(
                DeviceTypeRegistry.supportsBulkToggle(type),
                "$type should support bulk toggle",
            )
        }
    }

    @Test
    fun sensors_doNotSupportBulkToggle() {
        val noSupport = setOf(
            DeviceType.SMOKE_SENSOR,
            DeviceType.WATER_LEAK_SENSOR,
            DeviceType.TEMPERATURE_SENSOR,
            DeviceType.CONTACT_SENSOR,
        )

        noSupport.forEach { type ->
            assertFalse(
                DeviceTypeRegistry.supportsBulkToggle(type),
                "$type should NOT support bulk toggle",
            )
        }
    }

    // endregion

    // region Blind is bulk-toggleable but not individually toggleable

    @Test
    fun blind_isBulkToggleableButNotIndividuallyToggleable() {
        assertFalse(
            DeviceTypeRegistry.isToggleable(DeviceType.BLIND),
            "BLIND should NOT be individually toggleable",
        )
        assertTrue(
            DeviceTypeRegistry.supportsBulkToggle(DeviceType.BLIND),
            "BLIND should support bulk toggle",
        )
    }

    // endregion

    // region bulkToggleTypes set

    @Test
    fun bulkToggleTypes_containsExactlyExpectedTypes() {
        val expected = setOf(
            DeviceType.LIGHT,
            DeviceType.LOCK,
            DeviceType.SWITCH,
            DeviceType.SMART_TV,
            DeviceType.THERMOSTAT,
            DeviceType.BLIND,
        )

        kotlin.test.assertEquals(expected, DeviceTypeRegistry.bulkToggleTypes)
    }

    // endregion

    // region Every DeviceType is covered by exactly one branch

    @Test
    fun allDeviceTypes_areCoveredByRegistryMethods() {
        DeviceType.entries.forEach { type ->
            // These calls should not throw — every type must be handled
            DeviceTypeRegistry.isToggleable(type)
            DeviceTypeRegistry.supportsBulkToggle(type)
        }
    }

    // endregion
}
