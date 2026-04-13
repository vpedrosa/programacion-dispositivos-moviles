extends CanvasLayer

signal closed
signal powerup_purchased(powerup_id: String)

const POWERUPS: Dictionary = {
	"repair_city":   {"name": "Reparar ciudad",      "cost": 200, "desc": "Restaura vida a una ciudad danada"},
	"rebuild_city":  {"name": "Reconstruir ciudad",  "cost": 500, "desc": "Revive una ciudad destruida con HP minimo"},
	"shield":        {"name": "Escudo temporal",     "cost": 300, "desc": "Escudo en todas las ciudades (absorbe 1 impacto)"},
	"radius_plus":   {"name": "Radio explosion+",   "cost": 250, "desc": "Aumenta permanentemente el radio de explosion"},
	"double_shot":   {"name": "Disparo doble",       "cost": 350, "desc": "2 misiles por toque durante 15s"},
	"emp":           {"name": "Bomba EMP",           "cost": 600, "desc": "Destruye todos los misiles en pantalla"},
	"cooldown_plus": {"name": "Cadencia+",           "cost": 250, "desc": "Reduce permanentemente el cooldown de disparo"},
}

@onready var money_label: Label = $Panel/VBox/MoneyLabel
@onready var powerups_container: VBoxContainer = $Panel/VBox/PowerupsContainer

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
		row.theme_override_constants/separation = 12

		var info_box := VBoxContainer.new()
		info_box.size_flags_horizontal = Control.SIZE_EXPAND_FILL

		var name_label := Label.new()
		name_label.text = data["name"]

		var desc_label := Label.new()
		desc_label.text = data["desc"]

		var right_box := VBoxContainer.new()
		right_box.custom_minimum_size = Vector2(120, 0)

		var price_label := Label.new()
		price_label.text = "$" + str(data["cost"])
		price_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER

		var buy_btn := Button.new()
		buy_btn.text = "COMPRAR"
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
