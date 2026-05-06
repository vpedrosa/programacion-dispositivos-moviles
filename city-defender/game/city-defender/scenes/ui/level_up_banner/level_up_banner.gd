class_name LevelUpBanner
extends CanvasLayer

@onready var _sprite: Sprite2D = $Sprite2D

var _tween: Tween = null


func _ready() -> void:
	visible = false


func show_level_up() -> void:
	if _tween:
		_tween.kill()

	var viewport_size := get_viewport().get_visible_rect().size
	var center_x := viewport_size.x * 0.5
	var start_y := viewport_size.y * 0.86
	var end_y := viewport_size.y * 0.55

	_sprite.modulate = Color(0.0, 0.9, 0.25, 0.0)
	_sprite.position = Vector2(center_x, start_y)
	visible = true
	AudioManager.play_voice("wow" + str(randi_range(1, 4)))

	_tween = create_tween().set_parallel(true)
	_tween.tween_property(_sprite, "position:y", end_y, 1.7)
	_tween.tween_property(_sprite, "modulate:a", 1.0, 0.4)
	_tween.tween_property(_sprite, "modulate:a", 0.0, 0.5).set_delay(1.2)
	_tween.tween_callback(func() -> void: visible = false).set_delay(1.7)
