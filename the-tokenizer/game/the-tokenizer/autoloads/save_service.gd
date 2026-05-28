extends Node

## Persistencia del estado de juego con soporte multi-slot.
##
## Mantiene hasta [constant MAX_SLOTS] partidas independientes en
## `user://savegame_<slot>.json` y un slot activo cuyo identificador se
## persiste en `user://session.cfg` para que el autosave de 30 s y los hooks
## de cierre / pausa de la app sepan dónde escribir.

signal saved(slot: int)
signal save_failed(slot: int, reason: String)
signal active_slot_changed(slot: int)

const SAVE_VERSION := 1
const MAX_SLOTS := 3
const SESSION_PATH := "user://session.cfg"

@export var autosave_interval: float = 30.0

var _timer: Timer
var _pending: bool = false
var _current_slot: int = 0


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_load_active_slot()
	_timer = Timer.new()
	_timer.wait_time = autosave_interval
	_timer.autostart = true
	_timer.timeout.connect(_on_autosave)
	add_child(_timer)


func _notification(what: int) -> void:
	match what:
		NOTIFICATION_WM_CLOSE_REQUEST, NOTIFICATION_APPLICATION_PAUSED:
			if _current_slot > 0:
				save_now(_current_slot)


func get_active_slot() -> int:
	return _current_slot


func set_active_slot(slot: int) -> void:
	if not _is_valid_slot(slot) and slot != 0:
		push_error("SaveService: slot fuera de rango (%d)" % slot)
		return
	if _current_slot == slot:
		return
	_current_slot = slot
	_persist_active_slot()
	active_slot_changed.emit(slot)


func has_save(slot: int) -> bool:
	if not _is_valid_slot(slot):
		return false
	return FileAccess.file_exists(_slot_path(slot))


func save_now(slot: int = -1) -> bool:
	var target_slot := _current_slot if slot < 0 else slot
	if not _is_valid_slot(target_slot):
		return false
	var payload := {
		"version": SAVE_VERSION,
		"timestamp": Time.get_unix_time_from_system(),
		"state": GameState.state.to_dict(),
	}
	var file := FileAccess.open(_slot_path(target_slot), FileAccess.WRITE)
	if file == null:
		var reason := "FileAccess.open falló (err %d)" % FileAccess.get_open_error()
		save_failed.emit(target_slot, reason)
		push_error("SaveService: " + reason)
		return false
	file.store_string(JSON.stringify(payload))
	file.close()
	saved.emit(target_slot)
	return true


func load_save(slot: int) -> PlayerState:
	var data := _read_payload(slot)
	if data.is_empty():
		return null
	return PlayerState.from_dict(data.get("state", {}))


## Devuelve el slot con `timestamp` más alto entre los ocupados, o 0 si
## no hay ninguna partida guardada. En caso de empate gana el slot activo
## (`_current_slot`) — útil cuando dos snapshots se han escrito en el
## mismo segundo. Usado por el menú principal para que "Continuar"
## cargue directamente la última partida sin pasar por el selector.
func most_recent_slot() -> int:
	var best_slot := 0
	var best_ts := -1
	for slot in range(1, MAX_SLOTS + 1):
		if not has_save(slot):
			continue
		var meta := read_metadata(slot)
		var ts := int(meta.get("timestamp", 0))
		if ts > best_ts:
			best_ts = ts
			best_slot = slot
		elif ts == best_ts and slot == _current_slot:
			best_slot = slot
	return best_slot


func read_metadata(slot: int) -> Dictionary:
	var data := _read_payload(slot)
	if data.is_empty():
		return {}
	var state: Dictionary = data.get("state", {})
	return {
		"timestamp": int(data.get("timestamp", 0)),
		"era": int(state.get("current_era", PlayerState.ERA_BASEMENT)),
		"tokens": float(state.get("tokens", 0.0)),
		"qubits": int(state.get("qubits", 0)),
	}


func clear_save(slot: int) -> void:
	if not _is_valid_slot(slot) or not has_save(slot):
		return
	DirAccess.remove_absolute(_slot_path(slot))
	if _current_slot == slot:
		set_active_slot(0)


func _on_autosave() -> void:
	if _pending or _current_slot == 0:
		return
	_pending = true
	save_now(_current_slot)
	_pending = false


func _slot_path(slot: int) -> String:
	return "user://savegame_%d.json" % slot


func _is_valid_slot(slot: int) -> bool:
	return slot >= 1 and slot <= MAX_SLOTS


func _read_payload(slot: int) -> Dictionary:
	if not has_save(slot):
		return {}
	var file := FileAccess.open(_slot_path(slot), FileAccess.READ)
	if file == null:
		push_error("SaveService: no se pudo abrir el slot %d" % slot)
		return {}
	var text := file.get_as_text()
	file.close()
	var parsed: Variant = JSON.parse_string(text)
	if parsed == null or not (parsed is Dictionary):
		push_error("SaveService: slot %d corrupto" % slot)
		return {}
	var data: Dictionary = parsed
	if int(data.get("version", 0)) != SAVE_VERSION:
		push_warning("SaveService: slot %d con versión inesperada" % slot)
	return data


func _load_active_slot() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(SESSION_PATH) == OK:
		_current_slot = int(cfg.get_value("session", "active_slot", 0))


func _persist_active_slot() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("session", "active_slot", _current_slot)
	cfg.save(SESSION_PATH)
