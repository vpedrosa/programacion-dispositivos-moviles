class_name InterceptorMissile
extends Node2D


@export var speed: float = 600.0

var _target: Vector2
var _moving: bool = false
var _explosion_scene: PackedScene
var _explosion_radius: float = 80.0


func launch(from: Vector2, to: Vector2, explosion_scene: PackedScene, explosion_radius: float) -> void:
	global_position = from
	_target = to
	_explosion_scene = explosion_scene
	_explosion_radius = explosion_radius
	_moving = true
	_update_rotation()


func _process(delta: float) -> void:
	if not _moving:
		return
	var distance := global_position.distance_to(_target)
	var step := speed * delta
	if step >= distance:
		global_position = _target
		_moving = false
		_explode()
	else:
		global_position += global_position.direction_to(_target) * step


func _update_rotation() -> void:
	look_at(_target)
	rotation += PI / 2.0


func _explode() -> void:
	if _explosion_scene:
		var explosion: Explosion = _explosion_scene.instantiate()
		get_parent().add_child(explosion)
		explosion.init(global_position, _explosion_radius)
	queue_free()
