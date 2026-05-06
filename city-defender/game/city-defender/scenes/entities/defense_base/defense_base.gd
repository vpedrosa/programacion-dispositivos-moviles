class_name DefenseBase
extends Node2D

enum TurretState { IDLE, AIMING, COOLDOWN, RELOADING }

@export var missile_scene: PackedScene = preload("res://scenes/entities/missiles/interceptor_missile.tscn")
@export var explosion_scene: PackedScene = preload("res://scenes/entities/explosion/explosion.tscn")
@export var turret2_texture: Texture2D
const MIN_COOLDOWN: float = 0.1
const BASE_ROTATION_SPEED: float = 3.0

@export var base_cooldown: float = 0.5
@export var base_explosion_radius: float = 80.0

@onready var _sprite: Sprite2D = $Visual

var _state: TurretState = TurretState.IDLE
var _cooldown_timer: float = 0.0
var _explosion_radius_bonus: float = 0.0
var _cooldown_upgrades: int = 0

var _rotation_speed: float = BASE_ROTATION_SPEED
var _pending_target: Vector2 = Vector2.ZERO
var _aim_angle: float = 0.0

var _gatling_active: bool = false
var _is_holding: bool = false


func _process(delta: float) -> void:
	if _state == TurretState.COOLDOWN or _state == TurretState.RELOADING:
		_cooldown_timer += delta
		if _cooldown_timer >= base_cooldown:
			_cooldown_timer = 0.0
			if _state == TurretState.COOLDOWN:
				_state = TurretState.IDLE
			elif _state == TurretState.RELOADING:
				_fire_at_pending()

	if _state == TurretState.AIMING:
		var diff := angle_difference(_sprite.rotation, _aim_angle)
		var step := _rotation_speed * delta
		if abs(diff) <= step:
			_sprite.rotation = _aim_angle
			_fire_at_pending()
		else:
			_sprite.rotation += sign(diff) * step


func shoot_at(world_position: Vector2) -> void:
	if _state != TurretState.IDLE:
		return
	_pending_target = world_position
	_is_holding = true
	var dir := world_position - global_position
	_aim_angle = atan2(dir.x, -dir.y)
	_state = TurretState.AIMING


func release() -> void:
	_is_holding = false


func get_cooldown_progress() -> float:
	match _state:
		TurretState.COOLDOWN, TurretState.RELOADING:
			return _cooldown_timer / base_cooldown
		_:
			return 1.0


func add_explosion_radius(amount: float) -> void:
	_explosion_radius_bonus += amount


func reduce_cooldown(amount: float) -> void:
	base_cooldown = max(MIN_COOLDOWN, base_cooldown - amount)
	_cooldown_upgrades += 1
	if _cooldown_upgrades >= 3 and turret2_texture:
		_sprite.texture = turret2_texture


func upgrade_rotation_speed(amount: float) -> void:
	_rotation_speed += amount


func enable_gatling() -> void:
	_gatling_active = true


func _fire_at_pending() -> void:
	_launch_missile(_pending_target)
	_cooldown_timer = 0.0
	if _gatling_active and _is_holding:
		_aim_angle = atan2(
			(_pending_target - global_position).x,
			-(_pending_target - global_position).y
		)
		_state = TurretState.RELOADING
	else:
		_state = TurretState.COOLDOWN


func _launch_missile(target: Vector2) -> void:
	if not missile_scene:
		return
	var missile: InterceptorMissile = missile_scene.instantiate()
	get_parent().add_child(missile)
	missile.launch(global_position, target, explosion_scene, base_explosion_radius + _explosion_radius_bonus)
	AudioManager.play_sfx("launch")
