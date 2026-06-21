extends Control

## Tienda de mejoras como overlay.
##
## Se abre desde el HUD vía SceneManager.push_overlay. Lista las mejoras de
## la era actual usando UpgradeService y refresca afford y nivel al recibir
## señales de GameState. La sección de qubits sólo aparece cuando el jugador
## ha desbloqueado la mecánica cuántica (#321) — por ahora se mantiene
## oculta y se rellena cuando aterrice el catálogo de qubits.

const UPGRADE_CARD_SCENE := preload("res://scenes/ui/upgrade_card/upgrade_card.tscn")
const QUANTUM_EVENT_SCENE := preload("res://scenes/screens/quantum_event/quantum_event.tscn")

@onready var _close_button: Button = %CloseButton
@onready var _tokens_label: Label = %TokensLabel
@onready var _list: VBoxContainer = %UpgradeList
@onready var _empty_label: Label = %EmptyLabel
@onready var _qubit_section: Control = %QubitSection
@onready var _qubit_label: Label = %QubitLabel
@onready var _quantum_button: Button = %QuantumButton

var _rows: Dictionary = {}


func _ready() -> void:
	_close_button.pressed.connect(_close)
	_quantum_button.pressed.connect(_on_quantum_pressed)
	GameState.tokens_changed.connect(_on_tokens_changed)
	GameState.qubits_changed.connect(func(_v: int) -> void: _refresh_qubit_section())
	GameState.upgrade_levelup.connect(_on_upgrade_levelup)
	_refresh_tokens(GameState.state.tokens)
	_refresh_qubit_section()
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
	upgrades.sort_custom(_compare_upgrades)
	for upgrade in upgrades:
		var card := UPGRADE_CARD_SCENE.instantiate()
		_list.add_child(card)
		card.bind(upgrade)
		card.purchase_requested.connect(_on_purchase_requested)
		_rows[upgrade.id] = card


## Ordena la lista dejando cada prerrequisito por encima de quien lo exige.
##
## Prima la profundidad en la cadena de prerrequisitos: una mejora sin
## prerrequisito (profundidad 0) aparece siempre por encima de la que la
## necesita (profundidad >= 1), aunque esta última sea más barata — p.ej.
## era_1_serverroom (8000) cuelga de era_1_cluster (20000). A igualdad de
## profundidad se conserva el orden por coste.
func _compare_upgrades(a: UpgradeData, b: UpgradeData) -> bool:
	var depth_a := _prerequisite_depth(a)
	var depth_b := _prerequisite_depth(b)
	if depth_a != depth_b:
		return depth_a < depth_b
	return a.base_cost < b.base_cost


## Longitud de la cadena de prerrequisitos de una mejora (0 si no tiene).
## Acotada por seguridad para no colgarse si el catálogo tuviera un ciclo.
func _prerequisite_depth(upgrade: UpgradeData) -> int:
	var depth := 0
	var current := upgrade
	while current != null and current.prerequisite_id != &"" and depth < 32:
		current = UpgradeService.get_by_id(current.prerequisite_id)
		depth += 1
	return depth


func _on_purchase_requested(upgrade_id: StringName) -> void:
	if UpgradeService.try_purchase(upgrade_id):
		_refresh_all_rows()


func _on_tokens_changed(value: float) -> void:
	_refresh_tokens(value)
	_refresh_all_rows()
	# La disponibilidad del reinicio cuántico depende de lifetime_tokens, que
	# sigue creciendo con la generación pasiva mientras la tienda está abierta;
	# refrescamos para revelar el botón en cuanto se cruza el umbral.
	_refresh_qubit_section()


func _on_upgrade_levelup(_id: StringName, _level: int) -> void:
	_refresh_all_rows()


func _refresh_all_rows() -> void:
	for row in _rows.values():
		row.refresh()


func _refresh_tokens(value: float) -> void:
	_tokens_label.text = _format(value)


## Refresca la sección de qubits de la tienda.
##
## Si el reinicio cuántico está disponible (Era 7 + lifetime suficiente),
## muestra el botón de relanzamiento aunque el jugador ya lo haya rechazado:
## la oferta automática es de una sola vez por sesión, así que la tienda es el
## punto de reentrada. Si no está disponible pero el jugador ya tiene qubits
## (p.ej. de vuelta en Era 1 tras un reset), muestra sólo el recuento.
func _refresh_qubit_section() -> void:
	var qubits := GameState.state.qubits
	var available := QuantumService.is_available()
	_qubit_section.visible = available or qubits > 0
	if not _qubit_section.visible:
		return
	if available:
		var earned := QuantumService.qubits_on_reset()
		_qubit_label.text = "Realidad cuántica disponible · tienes %d qubits" % qubits
		_quantum_button.text = "REINICIAR CON QUBITS (+%d)" % earned
		_quantum_button.visible = true
	else:
		_qubit_label.text = "Qubits acumulados · %d" % qubits
		_quantum_button.visible = false


func _on_quantum_pressed() -> void:
	if not QuantumService.is_available():
		return
	SceneManager.push_overlay(QUANTUM_EVENT_SCENE)


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
