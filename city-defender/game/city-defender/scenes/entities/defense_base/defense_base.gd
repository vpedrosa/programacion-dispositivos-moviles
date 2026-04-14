class_name DefenseBase
extends Node2D

## Estados explícitos del cañón.
## IDLE:      listo para recibir un disparo.
## AIMING:    rotando hacia el objetivo (primer disparo o disparo manual).
## COOLDOWN:  misil lanzado, esperando recarga; rotación vuelve a neutro.
## RELOADING: modo Gatling — recargando Y re-apuntando simultáneamente.
enum TurretState { IDLE, AIMING, COOLDOWN, RELOADING }

@export var missile_scene: PackedScene = preload("res://scenes/entities/missiles/interceptor_missile.tscn")
@export var explosion_scene: PackedScene = preload("res://scenes/entities/explosion/explosion.tscn")
@export var turret2_texture: Texture2D
@export var base_cooldown: float = 0.5
@export var base_explosion_radius: float = 80.0

@onready var _sprite: Sprite2D = $Visual

var _state: TurretState = TurretState.IDLE
var _cooldown_timer: float = 0.0
var _explosion_radius_bonus: float = 0.0
var _cooldown_upgrades: int = 0

var _rotation_speed: float = 3.0  # rad/s
var _pending_target: Vector2 = Vector2.ZERO
var _aim_angle: float = 0.0

var _gatling_active: bool = false
var _is_holding: bool = false


func _process(delta: float) -> void:
	# Gestión del timer de cooldown y recarga.
	if _state == TurretState.COOLDOWN or _state == TurretState.RELOADING:
		_cooldown_timer += delta
		if _cooldown_timer >= _current_cooldown():
			_cooldown_timer = 0.0
			if _state == TurretState.COOLDOWN:
				_state = TurretState.IDLE
			elif _state == TurretState.RELOADING:
				_fire_at_pending()  # gatling: dispara el siguiente misil al terminar la recarga

	# Solo rota al apuntar; en el resto de estados mantiene el último ángulo.
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
	# Sprite apunta hacia arriba en rotation=0; fórmula: atan2(dx, -dy)
	_aim_angle = atan2(dir.x, -dir.y)
	_state = TurretState.AIMING


func release() -> void:
	_is_holding = false


func get_cooldown_progress() -> float:
	match _state:
		TurretState.COOLDOWN, TurretState.RELOADING:
			return _cooldown_timer / _current_cooldown()
		_:
			return 1.0


func add_explosion_radius(amount: float) -> void:
	_explosion_radius_bonus += amount


func reduce_cooldown(amount: float) -> void:
	base_cooldown = max(0.1, base_cooldown - amount)
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
		# En modo Gatling, re-apunta al mismo objetivo mientras el jugador mantiene pulsado.
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


func _current_cooldown() -> float:
	return base_cooldown
