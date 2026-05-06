## PlayerProfile — autoload que persiste el perfil del jugador en el dispositivo.
## El nombre se pide una sola vez en Game Over y se reutiliza en partidas posteriores.
class_name PlayerProfileAutoload
extends Node

const CONFIG_PATH := "user://profile.cfg"

var _player_name: String = ""


func _ready() -> void:
	_load()


# ── API pública ────────────────────────────────────────────────────────────────

func has_name() -> bool:
	return not _player_name.is_empty()


func get_player_name() -> String:
	return _player_name


## Saneado idéntico al flujo manual: trim + uppercase. Vacío no se guarda.
func set_player_name(value: String) -> void:
	var sanitized := value.strip_edges().to_upper()
	if sanitized.is_empty():
		return
	_player_name = sanitized
	_save()


## Borra el nombre guardado. Próxima partida pedirá el nombre como la primera vez.
func clear_name() -> void:
	_player_name = ""
	_save()


# ── Persistencia ───────────────────────────────────────────────────────────────

func _load() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(CONFIG_PATH) == OK:
		_player_name = cfg.get_value("profile", "name", "")


func _save() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("profile", "name", _player_name)
	cfg.save(CONFIG_PATH)
