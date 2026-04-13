class_name DefenseBase
extends Node2D

@export var missile_scene: PackedScene = preload("res://scenes/entities/missiles/interceptor_missile.tscn")
@export var explosion_scene: PackedScene = preload("res://scenes/entities/explosion/explosion.tscn")
@export var turret2_texture: Texture2D
@export var base_cooldown: float = 0.5
@export var base_explosion_radius: float = 80.0

@onready var _sprite: Sprite2D = $Visual

var _cooldown_timer: float = 0.0
var _can_shoot: bool = true
var _double_shot_active: bool = false
var _explosion_radius_bonus: float = 0.0
var _cooldown_upgrades: int = 0

var _rotation_speed: float = 3.0  # rad/s
var _is_aiming: bool = false
var _pending_target: Vector2 = Vector2.ZERO
var _aim_angle: float = 0.0


func _process(delta: float) -> void:
	# Cooldown
	if not _can_shoot:
		_cooldown_timer += delta
		if _cooldown_timer >= _current_cooldown():
			_can_shoot = true
			_cooldown_timer = 0.0

	# Rotate toward aim target; when reached, fire. Otherwise return to neutral.
	var target_rot := _aim_angle if _is_aiming else 0.0
	var diff := angle_difference(_sprite.rotation, target_rot)
	var step := _rotation_speed * delta
	if abs(diff) <= step:
		_sprite.rotation = target_rot
		if _is_aiming:
			_is_aiming = false
			_fire_at_pending()
	else:
		_sprite.rotation += sign(diff) * step


func shoot_at(world_position: Vector2) -> void:
	if not _can_shoot or _is_aiming:
		return
	_pending_target = world_position
	var dir := world_position - global_position
	# Sprite faces UP at rotation=0; formula to aim: atan2(dx, -dy)
	_aim_angle = atan2(dir.x, -dir.y)
	_is_aiming = true


func get_cooldown_progress() -> float:
	if _can_shoot:
		return 1.0
	return _cooldown_timer / _current_cooldown()


func add_explosion_radius(amount: float) -> void:
	_explosion_radius_bonus += amount


func reduce_cooldown(amount: float) -> void:
	base_cooldown = max(0.1, base_cooldown - amount)
	_cooldown_upgrades += 1
	if _cooldown_upgrades >= 3 and turret2_texture:
		_sprite.texture = turret2_texture


func upgrade_rotation_speed(amount: float) -> void:
	_rotation_speed += amount


func set_double_shot(active: bool) -> void:
	_double_shot_active = active


func _fire_at_pending() -> void:
	_launch_missile(_pending_target)
	if _double_shot_active:
		_launch_missile(_pending_target + Vector2(randf_range(-15, 15), randf_range(-15, 15)))
	_can_shoot = false
	_cooldown_timer = 0.0


func _launch_missile(target: Vector2) -> void:
	if not missile_scene:
		return
	var missile: InterceptorMissile = missile_scene.instantiate()
	get_parent().add_child(missile)
	missile.launch(global_position, target, explosion_scene, base_explosion_radius + _explosion_radius_bonus)
	AudioManager.play_sfx("launch")


func _current_cooldown() -> float:
	return base_cooldown
