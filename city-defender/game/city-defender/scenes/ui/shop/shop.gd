extends CanvasLayer

signal closed
signal powerup_purchased(powerup_id: String)

const POWERUPS: Dictionary = {
	"repair_city":   {"name": "PU_REPAIR_NAME",   "cost": 200, "desc": "PU_REPAIR_DESC"},
	"rebuild_city":  {"name": "PU_REBUILD_NAME",  "cost": 500, "desc": "PU_REBUILD_DESC"},
	"shield":        {"name": "PU_SHIELD_NAME",   "cost": 300, "desc": "PU_SHIELD_DESC"},
	"radius_plus":   {"name": "PU_RADIUS_NAME",   "cost": 250, "desc": "PU_RADIUS_DESC"},
	"double_shot":   {"name": "PU_DOUBLE_NAME",   "cost": 350, "desc": "PU_DOUBLE_DESC"},
	"emp":           {"name": "PU_EMP_NAME",       "cost": 600, "desc": "PU_EMP_DESC"},
	"cooldown_plus": {"name": "PU_COOLDOWN_NAME", "cost": 250, "desc": "PU_COOLDOWN_DESC"},
	"turret_speed":  {"name": "PU_TURRET_SPEED_NAME", "cost": 300, "desc": "PU_TURRET_SPEED_DESC"},
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

		var info_box := VBoxContainer.new()
		info_box.size_flags_horizontal = Control.SIZE_EXPAND_FILL

		var name_label := Label.new()
		name_label.text = tr(data["name"])

		var desc_label := Label.new()
		desc_label.text = tr(data["desc"])
		desc_label.add_theme_font_size_override("font_size", 18)

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
