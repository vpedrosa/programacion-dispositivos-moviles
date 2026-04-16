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

	_sprite.modulate = Color(0.0, 0.9, 0.25, 0.0)
	_sprite.position = Vector2(640.0, 620.0)
	visible = true
	AudioManager.play_voice("wow" + str(randi_range(1, 4)))

	_tween = create_tween()
	_tween.tween_property(_sprite, "position:y", 400.0, 1.7)

	_alpha_tween = create_tween()
	_alpha_tween.tween_property(_sprite, "modulate:a", 1.0, 0.4)
	_alpha_tween.tween_interval(0.8)
	_alpha_tween.tween_property(_sprite, "modulate:a", 0.0, 0.5)
	_alpha_tween.tween_callback(func() -> void: visible = false)
