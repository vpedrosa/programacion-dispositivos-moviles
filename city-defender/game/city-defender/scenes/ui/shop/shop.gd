extends CanvasLayer

signal closed
signal powerup_purchased(powerup_id: String)

const POWERUPS: Dictionary = {
	"repair_city":   {"name": "PU_REPAIR_NAME",   "cost": 200, "desc": "PU_REPAIR_DESC"},
	"rebuild_city":  {"name": "PU_REBUILD_NAME",  "cost": 500, "desc": "PU_REBUILD_DESC"},
	"shield":        {"name": "PU_SHIELD_NAME",   "cost": 200, "desc": "PU_SHIELD_DESC"},
	"radius_plus":   {"name": "PU_RADIUS_NAME",   "cost": 150, "desc": "PU_RADIUS_DESC"},
	"double_shot":   {"name": "PU_DOUBLE_NAME",   "cost": 150, "desc": "PU_DOUBLE_DESC"},
	"emp":           {"name": "PU_EMP_NAME",       "cost": 300, "desc": "PU_EMP_DESC"},
	"cooldown_plus": {"name": "PU_COOLDOWN_NAME", "cost": 50, "desc": "PU_COOLDOWN_DESC"},
	"turret_speed":  {"name": "PU_TURRET_SPEED_NAME", "cost": 60, "desc": "PU_TURRET_SPEED_DESC"},
}

const POWERUP_ICONS: Dictionary = {
	"repair_city":  "res://assets/sprites/shop/repair.png",
	"rebuild_city": "res://assets/sprites/shop/rebuild.png",
	"shield":       "res://assets/sprites/shop/shield.png",
	"radius_plus":  "res://assets/sprites/shop/radius.png",
	"double_shot":  "res://assets/sprites/shop/double.png",
	"emp":          "res://assets/sprites/shop/emp.png",
	"cooldown_plus":"res://assets/sprites/shop/cooldown.png",
	"turret_speed": "res://assets/sprites/shop/speed.png",
}

@onready var money_label: Label = $Panel/VBox/MoneyLabel
@onready var powerups_container: VBoxContainer = $Panel/VBox/ScrollContainer/PowerupsContainer

var _cities: Array = []
var _buy_buttons: Dictionary = {}


func _ready() -> void:
	visible = false
	_build_powerups()
	GameState.money_changed.connect(_on_money_changed)


func _build_powerups() -> void:
	for powerup_id in POWERUPS.keys():
		var data: Dictionary = POWERUPS[powerup_id]

		var row := HBoxContainer.new()
		row.add_theme_constant_override("separation", 12)

		# ── Icono enmarcado ──────────────────────────────────────────────────
		var icon_frame := Panel.new()
		icon_frame.custom_minimum_size = Vector2(64, 64)
		var style := StyleBoxFlat.new()
		style.bg_color = Color(0.0, 0.0, 0.0, 0.6)
		style.border_color = Color(0.0, 0.9, 0.25, 1.0)
		style.set_border_width_all(2)
		style.set_corner_radius_all(4)
		icon_frame.add_theme_stylebox_override("panel", style)

		var icon_rect := TextureRect.new()
		icon_rect.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT, Control.PRESET_MODE_MINSIZE, 6)
		icon_rect.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		icon_rect.expand_mode = TextureRect.EXPAND_FIT_WIDTH_PROPORTIONAL
		if POWERUP_ICONS.has(powerup_id):
			icon_rect.texture = load(POWERUP_ICONS[powerup_id])
		icon_frame.add_child(icon_rect)

		# ── Info ─────────────────────────────────────────────────────────────
		var info_box := VBoxContainer.new()
		info_box.size_flags_horizontal = Control.SIZE_EXPAND_FILL

		var name_label := Label.new()
		name_label.text = tr(data["name"])

		var desc_label := Label.new()
		desc_label.text = tr(data["desc"])
		desc_label.add_theme_font_size_override("font_size", 14)

		# ── Precio y botón ───────────────────────────────────────────────────
		var right_box := VBoxContainer.new()
		right_box.custom_minimum_size = Vector2(120, 0)

		var price_label := Label.new()
		price_label.text = "$" + str(data["cost"])
		price_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER

		var buy_btn := Button.new()
		buy_btn.text = tr("SHOP_BUY")
		buy_btn.pressed.connect(_on_buy_pressed.bind(powerup_id))
		_buy_buttons[powerup_id] = buy_btn

		info_box.add_child(name_label)
		info_box.add_child(desc_label)
		right_box.add_child(price_label)
		right_box.add_child(buy_btn)
		row.add_child(icon_frame)
		row.add_child(info_box)
		row.add_child(right_box)
		powerups_container.add_child(row)
		FalloutStyle.style_subtree(row)


func open(cities: Array) -> void:
	_cities = cities
	visible = true
	get_tree().paused = true
	_refresh_buttons()


func close() -> void:
	visible = false
	get_tree().paused = false
	closed.emit()


func _refresh_buttons() -> void:
	money_label.text = "$" + str(GameState.money)
	for powerup_id in _buy_buttons.keys():
		var btn: Button = _buy_buttons[powerup_id]
		btn.disabled = GameState.money < POWERUPS[powerup_id]["cost"]


func _on_money_changed(_new_money: int) -> void:
	if visible:
		_refresh_buttons()


func _on_buy_pressed(powerup_id: String) -> void:
	var cost: int = POWERUPS[powerup_id]["cost"]
	if GameState.spend_money(cost):
		powerup_purchased.emit(powerup_id)
		_refresh_buttons()


func _on_close_button_pressed() -> void:
	close()
