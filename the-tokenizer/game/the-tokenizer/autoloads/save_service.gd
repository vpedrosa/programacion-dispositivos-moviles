extends Node

## Persistencia del estado de juego.
##
## Guarda automáticamente cada [member autosave_interval] segundos y reacciona
## a NOTIFICATION_WM_CLOSE_REQUEST / NOTIFICATION_APPLICATION_PAUSED para no
## perder progreso al cerrar la app en Android.

signal saved()
signal save_failed(reason: String)

const SAVE_PATH := "user://savegame.json"
const SAVE_VERSION := 1

@export var autosave_interval: float = 30.0

var _timer: Timer
var _pending: bool = false


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_timer = Timer.new()
	_timer.wait_time = autosave_interval
	_timer.autostart = true
	_timer.timeout.connect(_on_autosave)
	add_child(_timer)


func _notification(what: int) -> void:
	match what:
		NOTIFICATION_WM_CLOSE_REQUEST, NOTIFICATION_APPLICATION_PAUSED:
			save_now()


func has_save() -> bool:
	return FileAccess.file_exists(SAVE_PATH)


func save_now() -> bool:
	var payload := {
		"version": SAVE_VERSION,
		"timestamp": Time.get_unix_time_from_system(),
		"state": GameState.state.to_dict(),
	}
	var file := FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file == null:
		var reason := "FileAccess.open falló (err %d)" % FileAccess.get_open_error()
		save_failed.emit(reason)
		push_error("SaveService: " + reason)
		return false
	file.store_string(JSON.stringify(payload))
	file.close()
	saved.emit()
	return true


func load_save() -> PlayerState:
	if not has_save():
		return null
	var file := FileAccess.open(SAVE_PATH, FileAccess.READ)
	if file == null:
		push_error("SaveService: no se pudo abrir el guardado")
		return null
	var text := file.get_as_text()
	file.close()
	var parsed: Variant = JSON.parse_string(text)
	if parsed == null or not (parsed is Dictionary):
		push_error("SaveService: guardado corrupto")
		return null
	var data: Dictionary = parsed
	if int(data.get("version", 0)) != SAVE_VERSION:
		push_warning("SaveService: versión de guardado no coincide (esperado %d)" % SAVE_VERSION)
	return PlayerState.from_dict(data.get("state", {}))


func clear_save() -> void:
	if not has_save():
		return
	DirAccess.remove_absolute(SAVE_PATH)


func _on_autosave() -> void:
	if _pending:
		return
	_pending = true
	save_now()
	_pending = false
