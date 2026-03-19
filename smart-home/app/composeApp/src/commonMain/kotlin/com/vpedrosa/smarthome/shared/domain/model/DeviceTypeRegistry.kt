package com.vpedrosa.smarthome.shared.domain.model

/**
 * Singleton registry that centralizes device type capabilities.
 *
 * Instead of scattering `when` branches across extension functions,
 * this object is the single source of truth for which device types
 * support toggling and bulk-toggle actions.
 *
 * This is a pure domain object with zero external dependencies,
 * implementing the Singleton pattern via Kotlin's `object` keyword.
 */
object DeviceTypeRegistry {

    /** Device types that support individual toggling (on/off or lock/unlock). */
    private val toggleableTypes: Set<DeviceType> = setOf(
        DeviceType.LIGHT,
        DeviceType.LOCK,
        DeviceType.SWITCH,
        DeviceType.SMART_TV,
        DeviceType.THERMOSTAT,
    )

    /** Device types that support bulk toggle actions (turn all on/off). */
    val bulkToggleTypes: Set<DeviceType> = setOf(
        DeviceType.LIGHT,
        DeviceType.LOCK,
        DeviceType.SWITCH,
        DeviceType.SMART_TV,
        DeviceType.THERMOSTAT,
        DeviceType.BLIND,
    )

    /**
     * Whether [type] supports toggling (on/off or lock/unlock).
     * Sensors and blinds do not have an individual toggle state.
     */
    fun isToggleable(type: DeviceType): Boolean = type in toggleableTypes

    /**
     * Whether [type] supports bulk toggle actions.
     * Contact sensors are individually toggleable but not bulk-toggled.
     */
    fun supportsBulkToggle(type: DeviceType): Boolean = type in bulkToggleTypes
}
