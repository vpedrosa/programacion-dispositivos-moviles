class_name City
extends Area2D

signal health_changed(new_health: int, max_health: int)
signal destroyed

const MAX_HEALTH: int = 100

@export var city_index: int = 0

var health: int = MAX_HEALTH
var is_alive: bool = true
var has_shield: bool = false


func _ready() -> void:
	add_to_group("cities")


func take_damage(amount: int = 25) -> void:
	if not is_alive:
		return
	if has_shield:
		has_shield = false
		_update_shield_visual(false)
		return
	health = max(0, health - amount)
	health_changed.emit(health, MAX_HEALTH)
	if health <= 0:
		_on_destroyed()


func heal(amount: int) -> void:
	if not is_alive:
		return
	health = min(MAX_HEALTH, health + amount)
	health_changed.emit(health, MAX_HEALTH)


func rebuild() -> void:
	health = 10
	is_alive = true
	_update_alive_visual(true)
	health_changed.emit(health, MAX_HEALTH)


func activate_shield() -> void:
	has_shield = true
	_update_shield_visual(true)


func _on_destroyed() -> void:
	is_alive = false
	_update_alive_visual(false)
	destroyed.emit()
	GameState.notify_city_destroyed()


func _update_alive_visual(_alive: bool) -> void:
	# TODO: swap sprite to destroyed state
	pass


func _update_shield_visual(_active: bool) -> void:
	# TODO: show/hide shield overlay node
	pass
