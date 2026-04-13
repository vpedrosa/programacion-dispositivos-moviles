extends Node

const PHOSPHOR := Color(0.0, 1.0, 0.25, 1)
const PHOSPHOR_BRIGHT := Color(0.4, 1.0, 0.55, 1)
const PHOSPHOR_DIM := Color(0.0, 0.7, 0.18, 1)

var _font: FontFile = null


func _ready() -> void:
	var path := "res://assets/fonts/Inconsolata-Regular.ttf"
	if ResourceLoader.exists(path):
		_font = load(path)


## Aplica el tema Fallout a toda la jerarquía de un Control y añade el overlay CRT.
func apply(root: Control) -> void:
	style_subtree(root)
	_add_scanline_overlay(root)


## Estiliza un nodo y sus descendientes sin añadir el overlay de scanlines.
## Útil para nodos creados dinámicamente después de llamar a apply().
func style_subtree(node: Node) -> void:
	_style_node(node)


func _style_node(node: Node) -> void:
	if node is Button:
		_style_button(node)
	elif node is Label:
		_style_label(node)
	elif node is LineEdit:
		_style_line_edit(node)
	elif node is ProgressBar:
		_style_progress_bar(node)
	for child in node.get_children():
		_style_node(child)


func _style_label(label: Label) -> void:
	label.add_theme_color_override("font_color", PHOSPHOR)
	label.add_theme_font_size_override("font_size", 24)
	label.uppercase = true
	if _font:
		label.add_theme_font_override("font", _font)


func _style_button(btn: Button) -> void:
	btn.add_theme_color_override("font_color", PHOSPHOR)
	btn.add_theme_color_override("font_hover_color", PHOSPHOR_BRIGHT)
	btn.add_theme_color_override("font_pressed_color", PHOSPHOR_DIM)
	btn.add_theme_font_size_override("font_size", 22)
	if _font:
		btn.add_theme_font_override("font", _font)

	if not btn.has_meta("_sfx_connected"):
		btn.set_meta("_sfx_connected", true)
		btn.pressed.connect(AudioManager.play_sfx.bind("button"))

	var normal := StyleBoxFlat.new()
	normal.bg_color = Color(0.0, 0.08, 0.02, 1)
	normal.border_color = PHOSPHOR
	normal.set_border_width_all(2)
	normal.set_corner_radius_all(0)
	normal.set_content_margin_all(8)
	btn.add_theme_stylebox_override("normal", normal)

	var hover := StyleBoxFlat.new()
	hover.bg_color = Color(0.0, 0.18, 0.05, 1)
	hover.border_color = PHOSPHOR_BRIGHT
	hover.set_border_width_all(2)
	hover.set_corner_radius_all(0)
	hover.set_content_margin_all(8)
	btn.add_theme_stylebox_override("hover", hover)

	var pressed := StyleBoxFlat.new()
	pressed.bg_color = Color(0.0, 0.35, 0.1, 1)
	pressed.border_color = PHOSPHOR_DIM
	pressed.set_border_width_all(2)
	pressed.set_corner_radius_all(0)
	pressed.set_content_margin_all(8)
	btn.add_theme_stylebox_override("pressed", pressed)


func _style_line_edit(le: LineEdit) -> void:
	le.add_theme_color_override("font_color", PHOSPHOR)
	le.add_theme_color_override("caret_color", PHOSPHOR_BRIGHT)
	le.add_theme_font_size_override("font_size", 22)
	if _font:
		le.add_theme_font_override("font", _font)

	var normal := StyleBoxFlat.new()
	normal.bg_color = Color(0.0, 0.05, 0.01, 1)
	normal.border_color = PHOSPHOR
	normal.set_border_width_all(2)
	normal.set_corner_radius_all(0)
	normal.set_content_margin_all(8)
	le.add_theme_stylebox_override("normal", normal)


func _style_progress_bar(bar: ProgressBar) -> void:
	var fill := StyleBoxFlat.new()
	fill.bg_color = PHOSPHOR
	bar.add_theme_stylebox_override("fill", fill)
	var bg := StyleBoxFlat.new()
	bg.bg_color = Color(0.0, 0.15, 0.04, 1)
	bg.border_color = PHOSPHOR_DIM
	bg.set_border_width_all(1)
	bar.add_theme_stylebox_override("background", bg)
	bar.add_theme_color_override("font_color", PHOSPHOR)


func _add_scanline_overlay(parent: Control) -> void:
	var layer := CanvasLayer.new()
	layer.layer = 10
	parent.add_child(layer)
	var rect := ColorRect.new()
	rect.mouse_filter = Control.MOUSE_FILTER_IGNORE
	rect.color = Color(0, 0, 0, 0)
	rect.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	var mat := ShaderMaterial.new()
	mat.shader = load("res://assets/shaders/scanlines.gdshader")
	rect.material = mat
	layer.add_child(rect)
