extends PanelContainer

## Fila de la tienda para una mejora concreta.
##
## La pantalla de tienda instancia una por cada UpgradeData de la era actual
## y se encarga de llamar refresh() cuando cambian tokens o nivel. La fila
## en sí es tonta: no escucha señales globales, sólo renderiza estado.

signal purchase_requested(upgrade_id: StringName)

@onready var _name_label: Label = %NameLabel
@onready var _level_label: Label = %LevelLabel
@onready var _description_label: Label = %DescriptionLabel
@onready var _effect_label: Label = %EffectLabel
@onready var _cost_label: Label = %CostLabel
@onready var _buy_button: Button = %BuyButton

var _upgrade: UpgradeData


func _ready() -> void:
	_buy_button.pressed.connect(_on_buy_pressed)


func bind(upgrade: UpgradeData) -> void:
	_upgrade = upgrade
	if not is_node_ready():
		await ready
	_name_label.text = upgrade.display_name
	_description_label.text = upgrade.description
	refresh()


func refresh() -> void:
	if _upgrade == null:
		return
	var level := GameState.get_upgrade_level(_upgrade.id)
	_level_label.text = _level_text(level)
	_effect_label.text = _effect_text(_upgrade)
	if _upgrade.is_max_level(level):
		_cost_label.text = ""
		_buy_button.text = "MAX"
		_buy_button.disabled = true
	else:
		var cost := _upgrade.cost_at_level(level)
		_cost_label.text = _format_cost(cost)
		_buy_button.text = "COMPRAR"
		_buy_button.disabled = GameState.state.tokens < cost or not UpgradeService.is_available(_upgrade.id)


func _on_buy_pressed() -> void:
	if _upgrade == null:
		return
	purchase_requested.emit(_upgrade.id)


func _level_text(level: int) -> String:
	if _upgrade.max_level == UpgradeData.LEVEL_INFINITE:
		return "Lv %d" % level
	if _upgrade.max_level == 1:
		return "Único" if level == 0 else "Adquirida"
	return "Lv %d / %d" % [level, _upgrade.max_level]


static func _effect_text(upgrade: UpgradeData) -> String:
	var value := upgrade.effect_value
	match upgrade.effect_type:
		UpgradeData.EffectType.TOKENS_PER_TAP_ADD:
			return "+%s / tap" % _trim(value)
		UpgradeData.EffectType.TOKENS_PER_TAP_MULT:
			return "x%s / tap" % _trim(value)
		UpgradeData.EffectType.TOKENS_PER_SECOND_ADD:
			return "+%s / s" % _trim(value)
		UpgradeData.EffectType.TOKENS_PER_SECOND_MULT:
			return "x%s / s" % _trim(value)
		UpgradeData.EffectType.QUBIT_MULTIPLIER_ADD:
			return "+%s qubit ×" % _trim(value)
		_:
			return ""


static func _trim(v: float) -> String:
	if v == floor(v):
		return "%d" % int(v)
	return "%.2f" % v


static func _format_cost(cost: float) -> String:
	if cost >= 1_000_000.0:
		return "%.1fM" % (cost / 1_000_000.0)
	if cost >= 1_000.0:
		return "%.1fk" % (cost / 1_000.0)
	if cost >= 100.0:
		return "%d" % roundi(cost)
	return "%.1f" % cost
