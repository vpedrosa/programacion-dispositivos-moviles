extends Control

## Overlay para elegir un hueco al empezar una partida nueva.
##
## Tras #367 "Continuar" ya carga directamente el slot más reciente sin
## pasar por aquí, así que esta pantalla solo aparece desde "Nueva":
## tap sobre un hueco vacío arranca la intro, tap sobre uno ocupado pide
## confirmación de sobrescritura. El botón de papelera siempre pide
## confirmación.

const INTRO_SCENE := "res://scenes/screens/intro/intro.tscn"

@onready var _title: Label = %TitleLabel
@onready var _back_button: Button = %BackButton
@onready var _slot_buttons: Array[Button] = [%SlotButton1, %SlotButton2, %SlotButton3]
@onready var _delete_buttons: Array[Button] = [%DeleteButton1, %DeleteButton2, %DeleteButton3]
@onready var _confirm_modal: ConfirmModal = %ConfirmModal

var _pending_action: Callable = Callable()


func _ready() -> void:
	_back_button.pressed.connect(_close)
	for i in range(SaveService.MAX_SLOTS):
		var slot := i + 1
		_slot_buttons[i].pressed.connect(_on_slot_pressed.bind(slot))
		_delete_buttons[i].pressed.connect(_on_delete_pressed.bind(slot))
	_confirm_modal.confirmed.connect(_run_pending_action)
	_confirm_modal.cancelled.connect(_cancel_pending_action)
	AudioManager.wire_buttons_in(self)
	refresh()


func refresh() -> void:
	_title.text = "ELIGE HUECO"
	for i in range(SaveService.MAX_SLOTS):
		var slot := i + 1
		var occupied := SaveService.has_save(slot)
		var meta := SaveService.read_metadata(slot) if occupied else {}
		_slot_buttons[i].text = _format_slot(slot, occupied, meta)
		_delete_buttons[i].visible = occupied


func _format_slot(slot: int, occupied: bool, meta: Dictionary) -> String:
	if not occupied:
		return "SLOT %d · vacío" % slot
	var era := int(meta.get("era", PlayerState.ERA_BASEMENT))
	var tokens := float(meta.get("tokens", 0.0))
	var ts := int(meta.get("timestamp", 0))
	return "SLOT %d · ERA %d · %s · %s" % [slot, era, _format_tokens(tokens), _format_relative_time(ts)]


static func _format_tokens(v: float) -> String:
	if v >= 1_000_000.0:
		return "%.1fM" % (v / 1_000_000.0)
	if v >= 1_000.0:
		return "%.1fk" % (v / 1_000.0)
	return "%d" % roundi(v)


static func _format_relative_time(ts: int) -> String:
	var now := int(Time.get_unix_time_from_system())
	var diff := maxi(0, now - ts)
	if diff < 60:
		return "ahora"
	if diff < 3600:
		@warning_ignore("integer_division")
		return "hace %d min" % (diff / 60)
	if diff < 86400:
		@warning_ignore("integer_division")
		return "hace %d h" % (diff / 3600)
	@warning_ignore("integer_division")
	return "hace %d d" % (diff / 86400)


func _on_slot_pressed(slot: int) -> void:
	if SaveService.has_save(slot):
		_pending_action = _start_new_in.bind(slot)
		_confirm_modal.set_content(
			"Sobrescribir partida",
			"El SLOT %d tiene una partida. ¿Sobrescribir y empezar de nuevo?" % slot,
			"Empezar",
			"Cancelar",
		)
		_confirm_modal.show_modal()
	else:
		_start_new_in(slot)


func _on_delete_pressed(slot: int) -> void:
	_pending_action = _delete_slot.bind(slot)
	_confirm_modal.set_content(
		"Borrar partida",
		"Vas a borrar la partida del SLOT %d. ¿Estás seguro?" % slot,
		"Borrar",
		"Cancelar",
	)
	_confirm_modal.show_modal()


func _run_pending_action() -> void:
	if _pending_action.is_valid():
		var action := _pending_action
		_pending_action = Callable()
		action.call()


func _cancel_pending_action() -> void:
	_pending_action = Callable()


func _start_new_in(slot: int) -> void:
	SaveService.clear_save(slot)
	SaveService.set_active_slot(slot)
	GameState.reset()
	SceneManager.pop_overlay()
	SceneManager.change_scene(INTRO_SCENE)


func _delete_slot(slot: int) -> void:
	SaveService.clear_save(slot)
	refresh()


func _close() -> void:
	SceneManager.pop_overlay()
