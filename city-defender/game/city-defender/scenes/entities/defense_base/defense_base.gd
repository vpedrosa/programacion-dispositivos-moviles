class_name DefenseBase
extends Node2D

@export var missile_scene: PackedScene = preload("res://scenes/entities/missiles/interceptor_missile.tscn")
@export var explosion_scene: PackedScene = preload("res://scenes/entities/explosion/explosion.tscn")
@export var base_cooldown: float = 0.5
@export var base_explosion_radius: float = 80.0

var _cooldown_timer: float = 0.0
var _can_shoot: bool = true
var _double_shot_active: bool = false
var _explosion_radius_bonus: float = 0.0


func _process(delta: float) -> void:
	if not _can_shoot:
		_cooldown_timer += delta
		var progress := _cooldown_timer / _current_cooldown()
		# Notify HUD each frame — game.gd polls this
		if _cooldown_timer >= _current_cooldown():
			_can_shoot = true
			_cooldown_timer = 0.0


func shoot_at(world_position: Vector2) -> void:
	if not _can_shoot:
		return
	_launch_missile(world_position)
	if _double_shot_active:
		_launch_missile(world_position + Vector2(randf_range(-15, 15), randf_range(-15, 15)))
	_can_shoot = false
	_cooldown_timer = 0.0


func get_cooldown_progress() -> float:
	if _can_shoot:
		return 1.0
	return _cooldown_timer / _current_cooldown()


func add_explosion_radius(amount: float) -> void:
	_explosion_radius_bonus += amount


func reduce_cooldown(amount: float) -> void:
	base_cooldown = max(0.1, base_cooldown - amount)


func set_double_shot(active: bool) -> void:
	_double_shot_active = active


func _launch_missile(target: Vector2) -> void:
	if not missile_scene:
		return
	var missile: InterceptorMissile = missile_scene.instantiate()
	get_parent().add_child(missile)
	missile.launch(global_position, target, explosion_scene, base_explosion_radius + _explosion_radius_bonus)
	AudioManager.play_sfx("launch")


func _current_cooldown() -> float:
	return base_cooldown
