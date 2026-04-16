class_name ShopItemRow
extends PanelContainer

signal buy_pressed

const BORDER_COLOR := Color(0.0, 0.9, 0.25, 1.0)
const BG_COLOR := Color(0.0, 0.04, 0.01, 1.0)

var name_label: Label
var desc_label: Label
var buy_btn: Button


func setup(display_name: String, description: String, cost: int, icon_path: String) -> void:
	_apply_panel_style()
	var row := HBoxContainer.new()
	row.add_theme_constant_override("separation", 0)
	row.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	row.add_child(_build_icon(icon_path))
	row.add_child(_build_info(display_name, description))
	row.add_child(_build_price(cost))
	buy_btn = _build_buy_button()
	row.add_child(buy_btn)
	add_child(row)
	FalloutStyle.style_subtree(self)
	name_label.add_theme_font_size_override("font_size", 24)
	desc_label.add_theme_font_size_override("font_size", 17)


func _apply_panel_style() -> void:
	var style := StyleBoxFlat.new()
	style.bg_color = BG_COLOR
	style.border_color = BORDER_COLOR
	style.set_border_width_all(1)
	style.content_margin_left   = 0.0
	style.content_margin_right  = 1.0
	style.content_margin_top    = 0.0
	style.content_margin_bottom = 0.0
	add_theme_stylebox_override("panel", style)


func _build_icon(icon_path: String) -> PanelContainer:
	var frame := PanelContainer.new()
	frame.custom_minimum_size = Vector2(56, 56)
	frame.size_flags_vertical = Control.SIZE_SHRINK_CENTER
	frame.add_theme_stylebox_override("panel", _right_border_style(8.0))
	var rect := TextureRect.new()
	rect.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	rect.size_flags_vertical = Control.SIZE_EXPAND_FILL
	rect.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
	rect.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
	if not icon_path.is_empty():
		rect.texture = load(icon_path)
	frame.add_child(rect)
	return frame


func _build_info(display_name: String, description: String) -> PanelContainer:
	var panel := PanelContainer.new()
	panel.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	panel.add_theme_stylebox_override("panel", _right_border_style(6.0, 4.0))
	var vbox := VBoxContainer.new()
	vbox.size_flags_vertical = Control.SIZE_SHRINK_CENTER
	vbox.add_theme_constant_override("separation", 2)
	name_label = Label.new()
	name_label.text = display_name
	desc_label = Label.new()
	desc_label.text = description
	desc_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	vbox.add_child(name_label)
	vbox.add_child(desc_label)
	panel.add_child(vbox)
	return panel


func _build_price(cost: int) -> PanelContainer:
	var panel := PanelContainer.new()
	panel.add_theme_stylebox_override("panel", _right_border_style(8.0))
	var label := Label.new()
	label.text = "$" + str(cost)
	label.custom_minimum_size.x = 74
	label.size_flags_vertical = Control.SIZE_SHRINK_CENTER
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.add_theme_font_size_override("font_size", 28)
	panel.add_child(label)
	return panel


func _build_buy_button() -> Button:
	var btn := Button.new()
	btn.custom_minimum_size = Vector2(120, 52)
	btn.size_flags_vertical = Control.SIZE_SHRINK_CENTER
	btn.pressed.connect(func() -> void: buy_pressed.emit())
	var btn_n := StyleBoxFlat.new()
	btn_n.bg_color = Color(0.0, 0.08, 0.02, 1.0)
	btn_n.set_border_width_all(0)
	var btn_h := StyleBoxFlat.new()
	btn_h.bg_color = Color(0.0, 0.18, 0.05, 1.0)
	btn_h.set_border_width_all(0)
	var btn_p := StyleBoxFlat.new()
	btn_p.bg_color = Color(0.0, 0.35, 0.10, 1.0)
	btn_p.set_border_width_all(0)
	btn.add_theme_stylebox_override("normal", btn_n)
	btn.add_theme_stylebox_override("hover", btn_h)
	btn.add_theme_stylebox_override("pressed", btn_p)
	btn.add_theme_stylebox_override("disabled", btn_n)
	btn.add_theme_stylebox_override("focus", StyleBoxEmpty.new())
	return btn


func _right_border_style(h_margin: float, v_margin: float = 0.0) -> StyleBoxFlat:
	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.0, 0.0, 0.0, 0.0)
	style.border_width_right = 1
	style.border_color = BORDER_COLOR
	style.content_margin_left   = h_margin
	style.content_margin_right  = h_margin
	style.content_margin_top    = v_margin
	style.content_margin_bottom = v_margin
	return style
