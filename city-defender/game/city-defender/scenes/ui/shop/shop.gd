class_name Shop
extends CanvasLayer

signal opened
signal closed
signal powerup_purchased(powerup_id: int)

const POWERUPS: Dictionary = {
	PowerupId.REPAIR_CITY:   {"name": "PU_REPAIR_NAME",   "cost": 200, "desc": "PU_REPAIR_DESC"},
	PowerupId.REBUILD_CITY:  {"name": "PU_REBUILD_NAME",  "cost": 500, "desc": "PU_REBUILD_DESC"},
	PowerupId.SHIELD:        {"name": "PU_SHIELD_NAME",   "cost": 200, "desc": "PU_SHIELD_DESC"},
	PowerupId.RADIUS_PLUS:   {"name": "PU_RADIUS_NAME",   "cost": 150, "desc": "PU_RADIUS_DESC"},
	PowerupId.GATLING:       {"name": "PU_GATLING_NAME",  "cost": 150, "desc": "PU_GATLING_DESC"},
	PowerupId.EMP:           {"name": "PU_EMP_NAME",       "cost": 300, "desc": "PU_EMP_DESC"},
	PowerupId.COOLDOWN_PLUS: {"name": "PU_COOLDOWN_NAME", "cost": 50,  "desc": "PU_COOLDOWN_DESC"},
	PowerupId.TURRET_SPEED:  {"name": "PU_TURRET_SPEED_NAME", "cost": 60, "desc": "PU_TURRET_SPEED_DESC"},
}

const POWERUP_ICONS: Dictionary = {
	PowerupId.REPAIR_CITY:   "res://assets/sprites/shop/repair.png",
	PowerupId.REBUILD_CITY:  "res://assets/sprites/shop/rebuild.png",
	PowerupId.SHIELD:        "res://assets/sprites/shop/shield.png",
	PowerupId.RADIUS_PLUS:   "res://assets/sprites/shop/radius.png",
	PowerupId.GATLING:       "res://assets/sprites/shop/double.png",
	PowerupId.EMP:           "res://assets/sprites/shop/emp.png",
	PowerupId.COOLDOWN_PLUS: "res://assets/sprites/shop/cooldown.png",
	PowerupId.TURRET_SPEED:  "res://assets/sprites/shop/speed.png",
}

const MAX_PURCHASES: Dictionary = {
	PowerupId.GATLING:       1,
	PowerupId.RADIUS_PLUS:   1,
	PowerupId.COOLDOWN_PLUS: 3,
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


func _notification(what: int) -> void:
	# La notificación se dispara también al entrar al árbol, antes de que
	# _ready resuelva los @onready. is_node_ready() filtra ese caso.
	if what == NOTIFICATION_TRANSLATION_CHANGED and is_node_ready() and visible:
		_refresh_translations()
		_refresh_buttons()


func _build_powerups() -> void:
	for powerup_id in POWERUPS.keys():
		var data: Dictionary = POWERUPS[powerup_id]
		var row := ShopItemRow.new()
		row.setup(tr(data["name"]), tr(data["desc"]), data["cost"], POWERUP_ICONS.get(powerup_id, ""))
		row.buy_pressed.connect(_on_buy_pressed.bind(powerup_id))
		powerups_container.add_child(row)
		_buy_buttons[powerup_id] = row.buy_btn
		_name_labels[powerup_id] = row.name_label
		_desc_labels[powerup_id] = row.desc_label


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


func _on_buy_pressed(powerup_id: int) -> void:
	var cost: int = POWERUPS[powerup_id]["cost"]
	if GameState.spend_money(cost):
		_purchased_count[powerup_id] = _purchased_count.get(powerup_id, 0) + 1
		powerup_purchased.emit(powerup_id)
		_refresh_buttons()


func _on_close_button_pressed() -> void:
	close()
