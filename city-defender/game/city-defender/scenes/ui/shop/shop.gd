class_name Shop
extends CanvasLayer

signal opened
signal closed
signal powerup_purchased(powerup_id: String)

const POWERUPS: Dictionary = {
	"repair_city":   {"name": "PU_REPAIR_NAME",   "cost": 200, "desc": "PU_REPAIR_DESC"},
	"rebuild_city":  {"name": "PU_REBUILD_NAME",  "cost": 500, "desc": "PU_REBUILD_DESC"},
	"shield":        {"name": "PU_SHIELD_NAME",   "cost": 200, "desc": "PU_SHIELD_DESC"},
	"radius_plus":   {"name": "PU_RADIUS_NAME",   "cost": 150, "desc": "PU_RADIUS_DESC"},
	"gatling":       {"name": "PU_GATLING_NAME",  "cost": 150, "desc": "PU_GATLING_DESC"},
	"emp":           {"name": "PU_EMP_NAME",       "cost": 300, "desc": "PU_EMP_DESC"},
	"cooldown_plus": {"name": "PU_COOLDOWN_NAME", "cost": 50,  "desc": "PU_COOLDOWN_DESC"},
	"turret_speed":  {"name": "PU_TURRET_SPEED_NAME", "cost": 60, "desc": "PU_TURRET_SPEED_DESC"},
}

const POWERUP_ICONS: Dictionary = {
	"repair_city":  "res://assets/sprites/shop/repair.png",
	"rebuild_city": "res://assets/sprites/shop/rebuild.png",
	"shield":       "res://assets/sprites/shop/shield.png",
	"radius_plus":  "res://assets/sprites/shop/radius.png",
	"gatling":      "res://assets/sprites/shop/double.png",
	"emp":          "res://assets/sprites/shop/emp.png",
	"cooldown_plus":"res://assets/sprites/shop/cooldown.png",
	"turret_speed": "res://assets/sprites/shop/speed.png",
}

const MAX_PURCHASES: Dictionary = {
	"gatling":       1,
	"radius_plus":   1,
	"cooldown_plus": 3,
}

@onready var title_label: Label = $Panel/VBox/TitleLabel
@onready var money_label: Label = $Panel/VBox/MoneyLabel
@onready var powerups_container: VBoxContainer = $Panel/VBox/ScrollContainer/PowerupsContainer

var _cities: Array[City] = []
var _buy_buttons: Dictionary = {}
var _name_labels: Dictionary = {}
var _desc_labels: Dictionary = {}
var _purchased_count: Dictionary = {}


func _ready() -> void:
	visible = false
	powerups_container.add_theme_constant_override("separation", 0)
	_build_powerups()
	GameState.money_changed.connect(_on_money_changed)


func _build_powerups() -> void:
	for powerup_id in POWERUPS.keys():
		var data: Dictionary = POWERUPS[powerup_id]

		# ── Fila con borde (sin margen entre filas) ──────────────────────────────
		var row_panel := PanelContainer.new()
		var panel_style := StyleBoxFlat.new()
		panel_style.bg_color = Color(0.0, 0.04, 0.01, 1.0)
		panel_style.border_color = Color(0.0, 0.9, 0.25, 1.0)
		panel_style.set_border_width_all(1)
		panel_style.content_margin_left   = 0.0
		panel_style.content_margin_right  = 1.0
		panel_style.content_margin_top    = 0.0
		panel_style.content_margin_bottom = 0.0
		row_panel.add_theme_stylebox_override("panel", panel_style)

		var row := HBoxContainer.new()
		row.add_theme_constant_override("separation", 0)
		row.size_flags_horizontal = Control.SIZE_EXPAND_FILL

		# ── Col 1: Icono cuadrado con borde derecho ─────────────────────────────
		var icon_frame := PanelContainer.new()
		icon_frame.custom_minimum_size = Vector2(56, 56)
		icon_frame.size_flags_vertical = Control.SIZE_SHRINK_CENTER
		var icon_style := StyleBoxFlat.new()
		icon_style.bg_color = Color(0.0, 0.0, 0.0, 0.0)
		icon_style.border_width_left   = 0
		icon_style.border_width_top    = 0
		icon_style.border_width_right  = 1
		icon_style.border_width_bottom = 0
		icon_style.border_color = Color(0.0, 0.9, 0.25, 1.0)
		icon_style.content_margin_left   = 8.0
		icon_style.content_margin_right  = 8.0
		icon_style.content_margin_top    = 8.0
		icon_style.content_margin_bottom = 8.0
		icon_frame.add_theme_stylebox_override("panel", icon_style)

		var icon_rect := TextureRect.new()
		icon_rect.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		icon_rect.size_flags_vertical = Control.SIZE_EXPAND_FILL
		icon_rect.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		icon_rect.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
		if POWERUP_ICONS.has(powerup_id):
			icon_rect.texture = load(POWERUP_ICONS[powerup_id])
		icon_frame.add_child(icon_rect)

		# ── Col 2: Nombre + Descripción con borde derecho ────────────────────────
		var info_panel := PanelContainer.new()
		info_panel.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		var info_style := StyleBoxFlat.new()
		info_style.bg_color = Color(0.0, 0.0, 0.0, 0.0)
		info_style.border_width_left   = 0
		info_style.border_width_top    = 0
		info_style.border_width_right  = 1
		info_style.border_width_bottom = 0
		info_style.border_color = Color(0.0, 0.9, 0.25, 1.0)
		info_style.content_margin_left   = 6.0
		info_style.content_margin_right  = 6.0
		info_style.content_margin_top    = 4.0
		info_style.content_margin_bottom = 4.0
		info_panel.add_theme_stylebox_override("panel", info_style)

		var info_box := VBoxContainer.new()
		info_box.size_flags_vertical = Control.SIZE_SHRINK_CENTER
		info_box.add_theme_constant_override("separation", 2)

		var name_label := Label.new()
		name_label.text = tr(data["name"])

		var desc_label := Label.new()
		desc_label.text = tr(data["desc"])
		desc_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART

		info_box.add_child(name_label)
		info_box.add_child(desc_label)
		info_panel.add_child(info_box)

		# ── Col 3: Precio con borde derecho ──────────────────────────────────────
		var price_panel := PanelContainer.new()
		var price_style := StyleBoxFlat.new()
		price_style.bg_color = Color(0.0, 0.0, 0.0, 0.0)
		price_style.border_width_left   = 0
		price_style.border_width_top    = 0
		price_style.border_width_right  = 1
		price_style.border_width_bottom = 0
		price_style.border_color = Color(0.0, 0.9, 0.25, 1.0)
		price_style.content_margin_left   = 8.0
		price_style.content_margin_right  = 8.0
		price_style.content_margin_top    = 0.0
		price_style.content_margin_bottom = 0.0
		price_panel.add_theme_stylebox_override("panel", price_style)

		var price_label := Label.new()
		price_label.text = "$" + str(data["cost"])
		price_label.custom_minimum_size.x = 74
		price_label.size_flags_vertical = Control.SIZE_SHRINK_CENTER
		price_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
		price_panel.add_child(price_label)

		# ── Col 4: Botón comprar ─────────────────────────────────────────────────
		var buy_btn := Button.new()
		buy_btn.custom_minimum_size = Vector2(120, 52)
		buy_btn.size_flags_vertical = Control.SIZE_SHRINK_CENTER
		buy_btn.pressed.connect(_on_buy_pressed.bind(powerup_id))

		_buy_buttons[powerup_id] = buy_btn
		_name_labels[powerup_id] = name_label
		_desc_labels[powerup_id] = desc_label

		row.add_child(icon_frame)
		row.add_child(info_panel)
		row.add_child(price_panel)
		row.add_child(buy_btn)
		row_panel.add_child(row)
		powerups_container.add_child(row_panel)

		FalloutStyle.style_subtree(row_panel)
		name_label.add_theme_font_size_override("font_size", 24)
		desc_label.add_theme_font_size_override("font_size", 17)
		price_label.add_theme_font_size_override("font_size", 28)
		# Estilos sin borde para el botón comprar (después de FalloutStyle para no ser sobreescritos)
		var btn_n := StyleBoxFlat.new()
		btn_n.bg_color = Color(0.0, 0.08, 0.02, 1.0)
		btn_n.set_border_width_all(0)
		var btn_h := StyleBoxFlat.new()
		btn_h.bg_color = Color(0.0, 0.18, 0.05, 1.0)
		btn_h.set_border_width_all(0)
		var btn_p := StyleBoxFlat.new()
		btn_p.bg_color = Color(0.0, 0.35, 0.10, 1.0)
		btn_p.set_border_width_all(0)
		buy_btn.add_theme_stylebox_override("normal", btn_n)
		buy_btn.add_theme_stylebox_override("hover", btn_h)
		buy_btn.add_theme_stylebox_override("pressed", btn_p)
		buy_btn.add_theme_stylebox_override("disabled", btn_n)
		buy_btn.add_theme_stylebox_override("focus", StyleBoxEmpty.new())


func open(cities: Array[City]) -> void:
	_cities = cities
	visible = true
	opened.emit()
	_refresh_translations()
	_refresh_buttons()


func close() -> void:
	visible = false
	closed.emit()


func _refresh_translations() -> void:
	for pid in _name_labels:
		_name_labels[pid].text = tr(POWERUPS[pid]["name"])
		_desc_labels[pid].text = tr(POWERUPS[pid]["desc"])
	for pid in _buy_buttons:
		(_buy_buttons[pid] as Button).text = tr("SHOP_BUY")


func _refresh_buttons() -> void:
	var money_str := "$" + str(GameState.money)
	title_label.text = tr("SHOP_TITLE") + " (" + money_str + ")"
	money_label.visible = false
	for powerup_id in _buy_buttons.keys():
		var btn: Button = _buy_buttons[powerup_id]
		var max_p: int = MAX_PURCHASES.get(powerup_id, -1)
		var count: int = _purchased_count.get(powerup_id, 0)
		if max_p > 0 and count >= max_p:
			btn.disabled = true
		else:
			btn.disabled = GameState.money < POWERUPS[powerup_id]["cost"]


func _on_money_changed(_new_money: int) -> void:
	if visible:
		_refresh_buttons()


func _on_buy_pressed(powerup_id: String) -> void:
	var cost: int = POWERUPS[powerup_id]["cost"]
	if GameState.spend_money(cost):
		_purchased_count[powerup_id] = _purchased_count.get(powerup_id, 0) + 1
		powerup_purchased.emit(powerup_id)
		_refresh_buttons()


func _on_close_button_pressed() -> void:
	close()
