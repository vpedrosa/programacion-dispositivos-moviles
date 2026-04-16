class_name ImpactExplosion
extends Node2D

@export var duration: float = 0.5

var _timer: float = 0.0

@onready var _visual: Sprite2D = $Visual


func _process(delta: float) -> void:
	_timer += delta
	var t: float = clamp(_timer / duration, 0.0, 1.0)
	_visual.frame = min(int(t * 10), 9)
	if _timer >= duration:
		queue_free()
