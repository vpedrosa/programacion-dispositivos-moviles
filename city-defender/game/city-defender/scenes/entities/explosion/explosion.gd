class_name Explosion
extends Area2D

signal finished

@export var duration: float = 0.8

var radius: float = 80.0
var _timer: float = 0.0
var _hits_delivered: Array = []

@onready var _visual: Sprite2D = $Visual


func init(pos: Vector2, explosion_radius: float) -> void:
	global_position = pos
	radius = explosion_radius
	collision_layer = 0
	collision_mask = 4
	var shape := CircleShape2D.new()
	shape.radius = radius
	$CollisionShape2D.shape = shape
	area_entered.connect(_on_area_entered)
	# Escala el sprite para que cubra el radio de explosión (sprite mide 24px de radio)
	_visual.scale = Vector2.ONE * (radius / 24.0)


func _process(delta: float) -> void:
	_timer += delta
	var t: float = clamp(_timer / duration, 0.0, 1.0)
	_visual.frame = min(int(t * 10), 9)
	if _timer >= duration:
		finished.emit()
		queue_free()


func _on_area_entered(area: Area2D) -> void:
	if area is EnemyMissile and area not in _hits_delivered:
		_hits_delivered.append(area)
		area.hit()
