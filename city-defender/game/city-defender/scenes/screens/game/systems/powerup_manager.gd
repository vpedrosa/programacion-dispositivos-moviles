class_name PowerupManager
extends Node

const RADIUS_BONUS: float = 30.0
const COOLDOWN_REDUCTION: float = 0.1
const SHIELD_DURATION: float = 15.0
const ROTATION_SPEED_BONUS: float = 1.5


func apply_powerup(powerup_id: String, cities: Array) -> void:
	match powerup_id:
		"repair_city":   _repair_city(cities)
		"rebuild_city":  _rebuild_city(cities)
		"shield":        _activate_shield(cities)
		"radius_plus":   _increase_radius()
		"gatling":       _enable_gatling()
		"emp":           _activate_emp()
		"cooldown_plus": _reduce_cooldown()
		"turret_speed":  _upgrade_rotation_speed()


func _repair_city(cities: Array) -> void:
	var damaged: Array = []
	for c in cities:
		if (c as City).is_alive and (c as City).health < City.MAX_HEALTH:
			damaged.append(c)
	if not damaged.is_empty():
		(damaged[0] as City).heal(50)


func _rebuild_city(cities: Array) -> void:
	var destroyed: Array = []
	for c in cities:
		if not (c as City).is_alive:
			destroyed.append(c)
	if not destroyed.is_empty():
		(destroyed[0] as City).rebuild()


func _activate_shield(cities: Array) -> void:
	for city in cities:
		if (city as City).is_alive and not (city as City).has_shield:
			city.activate_shield()


func _increase_radius() -> void:
	get_parent().defense_base.add_explosion_radius(RADIUS_BONUS)


func _enable_gatling() -> void:
	get_parent().defense_base.enable_gatling()


func _activate_emp() -> void:
	AudioManager.play_sfx("emp")
	for missile in get_tree().get_nodes_in_group("enemy_missiles"):
		(missile as EnemyMissile).hit()
		(missile as EnemyMissile).hit()
	get_parent().hud.set_emp_available(false)


func _reduce_cooldown() -> void:
	get_parent().defense_base.reduce_cooldown(COOLDOWN_REDUCTION)


func _upgrade_rotation_speed() -> void:
	get_parent().defense_base.upgrade_rotation_speed(ROTATION_SPEED_BONUS)
