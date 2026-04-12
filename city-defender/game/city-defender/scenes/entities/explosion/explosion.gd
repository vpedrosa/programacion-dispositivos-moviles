class_name Explosion
extends Area2D

signal finished

@export var duration: float = 0.5

var radius: float = 80.0
var _timer: float = 0.0
var _hits_delivered: Array = []


func init(pos: Vector2, explosion_radius: float) -> void:
	global_position = pos
	radius = explosion_radius
	var shape := CircleShape2D.new()
	shape.radius = radius
	$CollisionShape2D.shape = shape
	_animate_expansion()


func _process(delta: float) -> void:
	_timer += delta
	var t := _timer / duration
	scale = Vector2.ONE * t
	if _timer >= duration:
		finished.emit()
		queue_free()


func _animate_expansion() -> void:
	scale = Vector2.ZERO


func _on_area_entered(area: Area2D) -> void:
	if area is EnemyMissile and area not in _hits_delivered:
		_hits_delivered.append(area)
		area.hit()
