extends Control

## Tienda de mejoras como overlay.
##
## Se abre desde el HUD vía SceneManager.push_overlay. Lista las mejoras de
## la era actual usando UpgradeService y refresca afford y nivel al recibir
## señales de GameState. La sección de qubits sólo aparece cuando el jugador
## ha desbloqueado la mecánica cuántica (#321) — por ahora se mantiene
## oculta y se rellena cuando aterrice el catálogo de qubits.

const UPGRADE_ROW_SCENE := preload("res://scenes/ui/upgrade_row/upgrade_row.tscn")

@onready var _close_button: Button = %CloseButton
@onready var _tokens_label: Label = %TokensLabel
@onready var _list: VBoxContainer = %UpgradeList
@onready var _empty_label: Label = %EmptyLabel
@onready var _qubit_section: Control = %QubitSection

var _rows: Dictionary = {}


func _ready() -> void:
	_close_button.pressed.connect(_close)
	GameState.tokens_changed.connect(_on_tokens_changed)
	GameState.qubits_changed.connect(_refresh_qubit_section_visibility)
	GameState.upgrade_levelup.connect(_on_upgrade_levelup)
	_refresh_tokens(GameState.state.tokens)
	_refresh_qubit_section_visibility(GameState.state.qubits)
	_populate(GameState.state.current_era)
	AudioManager.wire_buttons_in(self)


func _populate(era: int) -> void:
	for child in _list.get_children():
		child.queue_free()
	_rows.clear()
	var upgrades := UpgradeService.get_for_era(era)
	if upgrades.is_empty():
		_empty_label.visible = true
		return
	_empty_label.visible = false
	upgrades.sort_custom(func(a, b): return a.base_cost < b.base_cost)
	for upgrade in upgrades:
		var row := UPGRADE_ROW_SCENE.instantiate()
		_list.add_child(row)
		row.bind(upgrade)
		row.purchase_requested.connect(_on_purchase_requested)
		_rows[upgrade.id] = row


func _on_purchase_requested(upgrade_id: StringName) -> void:
	if UpgradeService.try_purchase(upgrade_id):
		_refresh_all_rows()


func _on_tokens_changed(value: float) -> void:
	_refresh_tokens(value)
	_refresh_all_rows()


func _on_upgrade_levelup(_id: StringName, _level: int) -> void:
	_refresh_all_rows()


func _refresh_all_rows() -> void:
	for row in _rows.values():
		row.refresh()


func _refresh_tokens(value: float) -> void:
	_tokens_label.text = _format(value)


func _refresh_qubit_section_visibility(qubits: int) -> void:
	_qubit_section.visible = qubits > 0


func _close() -> void:
	SceneManager.pop_overlay()


static func _format(value: float) -> String:
	if value >= 1_000_000.0:
		return "%.2fM" % (value / 1_000_000.0)
	if value >= 1_000.0:
		return "%.1fk" % (value / 1_000.0)
	if value >= 100.0:
		return "%d" % roundi(value)
	return "%.1f" % value
