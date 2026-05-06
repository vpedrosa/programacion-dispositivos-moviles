class_name PowerupManager
extends Node

const RADIUS_BONUS: float = 30.0
const COOLDOWN_REDUCTION: float = 0.1
const ROTATION_SPEED_BONUS: float = 1.5

@export var defense_base: DefenseBase
@export var hud: HUD


func apply_powerup(powerup_id: int, cities: Array[City]) -> void:
	match powerup_id:
		PowerupId.REPAIR_CITY:   _repair_city(cities)
		PowerupId.REBUILD_CITY:  _rebuild_city(cities)
		PowerupId.SHIELD:        _activate_shield(cities)
		PowerupId.RADIUS_PLUS:   _increase_radius()
		PowerupId.GATLING:       _enable_gatling()
		PowerupId.EMP:           _activate_emp()
		PowerupId.COOLDOWN_PLUS: _reduce_cooldown()
		PowerupId.TURRET_SPEED:  _upgrade_rotation_speed()


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
	AudioManager.play_zap("zap")
	FalloutStyle.flash_screen(get_parent())
	for missile: EnemyMissile in get_tree().get_nodes_in_group("enemy_missiles"):
		missile.kill()
	if hud:
		hud.set_emp_available(false)


func _reduce_cooldown() -> void:
	if defense_base:
		defense_base.reduce_cooldown(COOLDOWN_REDUCTION)


func _upgrade_rotation_speed() -> void:
	if defense_base:
		defense_base.upgrade_rotation_speed(ROTATION_SPEED_BONUS)
