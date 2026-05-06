class_name LevelUpBanner
extends CanvasLayer

@onready var _sprite: Sprite2D = $Sprite2D

var _tween: Tween = null
var _alpha_tween: Tween = null


func _ready() -> void:
	visible = false


func show_level_up() -> void:
	if _tween:
		_tween.kill()
	if _alpha_tween:
		_alpha_tween.kill()

	var viewport_size := get_viewport().get_visible_rect().size
	var center_x := viewport_size.x * 0.5
	var start_y := viewport_size.y * 0.86
	var end_y := viewport_size.y * 0.55

	_sprite.modulate = Color(0.0, 0.9, 0.25, 0.0)
	_sprite.position = Vector2(center_x, start_y)
	visible = true
	AudioManager.play_voice("wow" + str(randi_range(1, 4)))

	_tween = create_tween()
	_tween.tween_property(_sprite, "position:y", end_y, 1.7)

	_alpha_tween = create_tween()
	_alpha_tween.tween_property(_sprite, "modulate:a", 1.0, 0.4)
	_alpha_tween.tween_interval(0.8)
	_alpha_tween.tween_property(_sprite, "modulate:a", 0.0, 0.5)
	_alpha_tween.tween_callback(func() -> void: visible = false)
