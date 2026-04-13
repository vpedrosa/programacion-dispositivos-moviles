extends Control


func _ready() -> void:
	CursorManager.set_menu_cursor()
	$Logo.modulate.a = 0.0
	_animate()


func _animate() -> void:
	var tween := create_tween()
	tween.tween_property($Logo, "modulate:a", 1.0, 0.9)   # fade in
	tween.tween_interval(1.6)                               # mostrar
	tween.tween_property($Logo, "modulate:a", 0.0, 0.7)   # fade out
	tween.tween_callback(
		func() -> void:
			get_tree().change_scene_to_file("res://scenes/screens/main_menu/main_menu.tscn")
	)
