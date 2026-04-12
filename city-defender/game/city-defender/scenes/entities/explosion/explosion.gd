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
	collision_layer = 0
	collision_mask = 4   # detect enemy_missiles layer
	var shape := CircleShape2D.new()
	shape.radius = radius
	$CollisionShape2D.shape = shape
	area_entered.connect(_on_area_entered)
	_build_visual(radius)
	_animate_expansion()


func _build_visual(r: float) -> void:
	var poly := Polygon2D.new()
	var points := PackedVector2Array()
	var segments := 24
	for i in range(segments):
		var angle := (TAU / segments) * i
		points.append(Vector2(cos(angle), sin(angle)) * r)
	poly.polygon = points
	poly.color = Color(0.0, 1.0, 0.25, 0.5)
	add_child(poly)


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
