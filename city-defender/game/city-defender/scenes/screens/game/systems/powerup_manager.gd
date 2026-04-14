class_name PowerupManager
extends Node

const RADIUS_BONUS: float = 30.0
const COOLDOWN_REDUCTION: float = 0.1
const ROTATION_SPEED_BONUS: float = 1.5

var defense_base: DefenseBase
var hud: CanvasLayer


func _ready() -> void:
	defense_base = get_parent().get_node_or_null("DefenseBase") as DefenseBase
	hud = get_parent().get_node_or_null("HUD") as CanvasLayer
	if defense_base == null:
		push_error("PowerupManager: DefenseBase no encontrado")
	if hud == null:
		push_error("PowerupManager: HUD no encontrado")


func apply_powerup(powerup_id: String, cities: Array[City]) -> void:
	match powerup_id:
		"repair_city":   _repair_city(cities)
		"rebuild_city":  _rebuild_city(cities)
		"shield":        _activate_shield(cities)
		"radius_plus":   _increase_radius()
		"gatling":       _enable_gatling()
		"emp":           _activate_emp()
		"cooldown_plus": _reduce_cooldown()
		"turret_speed":  _upgrade_rotation_speed()


func _repair_city(cities: Array[City]) -> void:
	var damaged: Array[City] = []
	for c: City in cities:
		if c.is_alive and c.health < City.MAX_HEALTH:
			damaged.append(c)
	if not damaged.is_empty():
		damaged[0].heal(50)


func _rebuild_city(cities: Array[City]) -> void:
	var destroyed: Array[City] = []
	for c: City in cities:
		if not c.is_alive:
			destroyed.append(c)
	if not destroyed.is_empty():
		destroyed[0].rebuild()


func _activate_shield(cities: Array[City]) -> void:
	for c: City in cities:
		if c.is_alive and not c.has_shield:
			c.activate_shield()


func _increase_radius() -> void:
	if defense_base:
		defense_base.add_explosion_radius(RADIUS_BONUS)


func _enable_gatling() -> void:
	if defense_base:
		defense_base.enable_gatling()


func _activate_emp() -> void:
	AudioManager.play_sfx("emp")
	for missile: EnemyMissile in get_tree().get_nodes_in_group("enemy_missiles"):
		missile.hit()
		missile.hit()
	if hud:
		hud.set_emp_available(false)


func _reduce_cooldown() -> void:
	if defense_base:
		defense_base.reduce_cooldown(COOLDOWN_REDUCTION)


func _upgrade_rotation_speed() -> void:
	if defense_base:
		defense_base.upgrade_rotation_speed(ROTATION_SPEED_BONUS)
