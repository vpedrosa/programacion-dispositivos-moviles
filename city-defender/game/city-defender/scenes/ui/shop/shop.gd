class_name Shop
extends CanvasLayer

signal opened
signal closed
signal powerup_purchased(powerup_id: int)

@export var game_screen: GameScreen

@onready var title_label: Label = $Panel/VBox/TitleLabel
@onready var powerups_container: VBoxContainer = $Panel/VBox/ScrollContainer/PowerupsContainer

var _powerups: Array[PowerupData] = []
var _cities: Array[City] = []
var _rows: Dictionary = {}
var _purchased_count: Dictionary = {}


func _ready() -> void:
	visible = false
	_powerups = _build_powerup_defs()
	powerups_container.add_theme_constant_override("separation", 0)
	_build_powerups()
	if game_screen:
		game_screen.money_changed.connect(_on_money_changed)


func _notification(what: int) -> void:
	if what == NOTIFICATION_TRANSLATION_CHANGED and is_node_ready() and visible:
		_refresh_translations()
		_refresh_buttons()


func _build_powerup_defs() -> Array[PowerupData]:
	var arr: Array[PowerupData] = []
	arr.append(PowerupData.make(PowerupId.REPAIR_CITY,   "PU_REPAIR_NAME",       "PU_REPAIR_DESC",       200, "res://assets/sprites/shop/repair.png"))
	arr.append(PowerupData.make(PowerupId.REBUILD_CITY,  "PU_REBUILD_NAME",      "PU_REBUILD_DESC",      500, "res://assets/sprites/shop/rebuild.png"))
	arr.append(PowerupData.make(PowerupId.SHIELD,        "PU_SHIELD_NAME",       "PU_SHIELD_DESC",       200, "res://assets/sprites/shop/shield.png"))
	arr.append(PowerupData.make(PowerupId.RADIUS_PLUS,   "PU_RADIUS_NAME",       "PU_RADIUS_DESC",       150, "res://assets/sprites/shop/radius.png", 1))
	arr.append(PowerupData.make(PowerupId.GATLING,       "PU_GATLING_NAME",      "PU_GATLING_DESC",      150, "res://assets/sprites/shop/double.png", 1))
	arr.append(PowerupData.make(PowerupId.EMP,           "PU_EMP_NAME",          "PU_EMP_DESC",          300, "res://assets/sprites/shop/emp.png"))
	arr.append(PowerupData.make(PowerupId.COOLDOWN_PLUS, "PU_COOLDOWN_NAME",     "PU_COOLDOWN_DESC",     50,  "res://assets/sprites/shop/cooldown.png", 3))
	arr.append(PowerupData.make(PowerupId.TURRET_SPEED,  "PU_TURRET_SPEED_NAME", "PU_TURRET_SPEED_DESC", 60,  "res://assets/sprites/shop/speed.png"))
	return arr


func _build_powerups() -> void:
	for data: PowerupData in _powerups:
		var row := ShopItemRow.new()
		var icon_path := data.icon.resource_path if data.icon else ""
		row.setup(tr(data.name_key), tr(data.desc_key), data.cost, icon_path)
		row.buy_pressed.connect(_on_buy_pressed.bind(data.id))
		powerups_container.add_child(row)
		_rows[data.id] = row


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
	for data: PowerupData in _powerups:
		var row: ShopItemRow = _rows[data.id]
		row.name_label.text = tr(data.name_key)
		row.desc_label.text = tr(data.desc_key)
		row.buy_btn.text = tr("SHOP_BUY")


func _refresh_buttons() -> void:
	var money := game_screen.get_money() if game_screen else 0
	var money_str := "$" + str(money)
	title_label.text = tr("SHOP_TITLE") + " (" + money_str + ")"
	for data: PowerupData in _powerups:
		var btn: Button = (_rows[data.id] as ShopItemRow).buy_btn
		var count: int = _purchased_count.get(data.id, 0)
		if data.max_purchases > 0 and count >= data.max_purchases:
			btn.disabled = true
		else:
			btn.disabled = money < data.cost


func _on_money_changed(_new_money: int) -> void:
	if visible:
		_refresh_buttons()


func _on_buy_pressed(powerup_id: int) -> void:
	var data := _find_powerup(powerup_id)
	if data == null or game_screen == null:
		return
	if game_screen.spend_money(data.cost):
		_purchased_count[powerup_id] = _purchased_count.get(powerup_id, 0) + 1
		powerup_purchased.emit(powerup_id)
		_refresh_buttons()


func _find_powerup(powerup_id: int) -> PowerupData:
	for data: PowerupData in _powerups:
		if data.id == powerup_id:
			return data
	return null


func _on_close_button_pressed() -> void:
	close()
