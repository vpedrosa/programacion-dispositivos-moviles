extends Control

@onready var _play_btn: Button = $CenterContainer/VBox/PlayButton
@onready var _scores_btn: Button = $CenterContainer/VBox/HighscoresButton
@onready var _quit_btn: Button = $CenterContainer/VBox/QuitButton
@onready var _title: Label = $CenterContainer/VBox/Title


func _ready() -> void:
	_play_btn.pressed.connect(_on_play_pressed)
	_scores_btn.pressed.connect(_on_highscores_pressed)
	_quit_btn.pressed.connect(_on_quit_pressed)
	_apply_fallout_style()
	_add_scanline_overlay()


func _on_play_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/game/game.tscn")


func _on_highscores_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/highscores/highscores.tscn")


func _on_quit_pressed() -> void:
	get_tree().quit()


func _apply_fallout_style() -> void:
	var phosphor := Color(0.0, 1.0, 0.25, 1)
	var phosphor_bright := Color(0.4, 1.0, 0.55, 1)
	var phosphor_dim := Color(0.0, 0.7, 0.18, 1)

	_title.add_theme_color_override("font_color", phosphor)
	_title.add_theme_font_size_override("font_size", 56)

	for btn: Button in [_play_btn, _scores_btn, _quit_btn]:
		btn.add_theme_color_override("font_color", phosphor)
		btn.add_theme_color_override("font_hover_color", phosphor_bright)
		btn.add_theme_color_override("font_pressed_color", phosphor_dim)

		var normal := StyleBoxFlat.new()
		normal.bg_color = Color(0.0, 0.08, 0.02, 1)
		normal.border_color = phosphor
		normal.set_border_width_all(2)
		normal.set_corner_radius_all(0)
		normal.set_content_margin_all(8)
		btn.add_theme_stylebox_override("normal", normal)

		var hover := StyleBoxFlat.new()
		hover.bg_color = Color(0.0, 0.18, 0.05, 1)
		hover.border_color = phosphor_bright
		hover.set_border_width_all(2)
		hover.set_corner_radius_all(0)
		hover.set_content_margin_all(8)
		btn.add_theme_stylebox_override("hover", hover)

		var pressed := StyleBoxFlat.new()
		pressed.bg_color = Color(0.0, 0.35, 0.1, 1)
		pressed.border_color = phosphor_dim
		pressed.set_border_width_all(2)
		pressed.set_corner_radius_all(0)
		pressed.set_content_margin_all(8)
		btn.add_theme_stylebox_override("pressed", pressed)


func _add_scanline_overlay() -> void:
	var layer := CanvasLayer.new()
	layer.layer = 10
	add_child(layer)
	var rect := ColorRect.new()
	rect.mouse_filter = Control.MOUSE_FILTER_IGNORE
	rect.color = Color(0, 0, 0, 0)
	rect.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	var mat := ShaderMaterial.new()
	mat.shader = load("res://assets/shaders/scanlines.gdshader")
	rect.material = mat
	layer.add_child(rect)
