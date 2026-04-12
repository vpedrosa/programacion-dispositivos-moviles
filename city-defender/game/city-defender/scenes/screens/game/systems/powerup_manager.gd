class_name PowerupManager
extends Node

const RADIUS_BONUS: float = 30.0
const COOLDOWN_REDUCTION: float = 0.1
const SHIELD_DURATION: float = 15.0
const DOUBLE_SHOT_DURATION: float = 15.0

var _double_shot_timer: float = 0.0


func _process(delta: float) -> void:
	if _double_shot_timer > 0.0:
		_double_shot_timer -= delta
		get_parent().hud.show_powerup_active("Disparo doble", _double_shot_timer, DOUBLE_SHOT_DURATION)
		if _double_shot_timer <= 0.0:
			get_parent().defense_base.set_double_shot(false)


func apply_powerup(powerup_id: String, cities: Array) -> void:
	match powerup_id:
		"repair_city":   _repair_city(cities)
		"rebuild_city":  _rebuild_city(cities)
		"shield":        _activate_shield(cities)
		"radius_plus":   _increase_radius()
		"double_shot":   _activate_double_shot()
		"emp":           _activate_emp()
		"cooldown_plus": _reduce_cooldown()


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
		if (city as City).is_alive:
			city.activate_shield()


func _increase_radius() -> void:
	get_parent().defense_base.add_explosion_radius(RADIUS_BONUS)


func _activate_double_shot() -> void:
	_double_shot_timer = DOUBLE_SHOT_DURATION
	get_parent().defense_base.set_double_shot(true)


func _activate_emp() -> void:
	for missile in get_tree().get_nodes_in_group("enemy_missiles"):
		(missile as EnemyMissile).hit()
		(missile as EnemyMissile).hit()
	get_parent().hud.set_emp_available(false)


func _reduce_cooldown() -> void:
	get_parent().defense_base.reduce_cooldown(COOLDOWN_REDUCTION)
