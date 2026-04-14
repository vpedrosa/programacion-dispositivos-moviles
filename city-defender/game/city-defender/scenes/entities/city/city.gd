class_name City
extends Area2D

signal health_changed(new_health: int, max_health: int)
signal destroyed

const MAX_HEALTH: int = 100

@export var city_index: int = 0

var health: int = MAX_HEALTH
var is_alive: bool = true
var has_shield: bool = false

const FIRE_FPS: float = 8.0
const _ALIVE_TEX: Texture2D = preload("res://assets/sprites/cities/city_alive.png")
const _DEAD_TEX: Texture2D  = preload("res://assets/sprites/cities/city_destroyed.png")

var _fire_timer: float = 0.0

@onready var _visual: Sprite2D   = $Visual
@onready var _fire_back: Sprite2D  = $FireBack
@onready var _fire_front: Sprite2D = $FireFront
@onready var _fires: Array[Sprite2D] = []


func _ready() -> void:
	add_to_group("cities")
	collision_layer = 1
	collision_mask = 0
	var shape := RectangleShape2D.new()
	shape.size = Vector2(60, 30)
	$CollisionShape2D.shape = shape
	$CollisionShape2D.position = Vector2(0, -15)
	_fires = [_fire_back, _fire_front]


func take_damage(amount: int = 25) -> void:
	if not is_alive:
		return
	if has_shield:
		has_shield = false
		_update_shield_visual(false)
		AudioManager.play_zap("zap")
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
	GameState.notify_city_rebuilt()


func activate_shield() -> void:
	if has_shield:
		return
	has_shield = true
	_update_shield_visual(true)


func _on_destroyed() -> void:
	is_alive = false
	_update_alive_visual(false)
	destroyed.emit()
	AudioManager.play_sfx("city-destroyed")
	GameState.notify_city_destroyed()


func _process(delta: float) -> void:
	if _fires.is_empty() or not _fires[0].visible:
		return
	_fire_timer += delta
	if _fire_timer >= 1.0 / FIRE_FPS:
		_fire_timer = 0.0
		for fire in _fires:
			fire.frame = (fire.frame + 1) % fire.hframes


func _update_alive_visual(alive: bool) -> void:
	_visual.texture = _ALIVE_TEX if alive else _DEAD_TEX
	for fire: Sprite2D in _fires:
		fire.visible = not alive


func _update_shield_visual(active: bool) -> void:
	$ShieldEffect.visible = active
