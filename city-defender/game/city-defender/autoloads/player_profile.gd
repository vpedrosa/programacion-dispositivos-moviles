extends Node

const CONFIG_PATH := "user://profile.cfg"

var _player_name: String = ""


func _ready() -> void:
	_load()


func has_name() -> bool:
	return not _player_name.is_empty()


func get_player_name() -> String:
	return _player_name


func set_player_name(value: String) -> void:
	var sanitized := value.strip_edges().to_upper()
	if sanitized.is_empty():
		return
	_player_name = sanitized
	_save()


func clear_name() -> void:
	_player_name = ""
	_save()


func _load() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(CONFIG_PATH) == OK:
		_player_name = cfg.get_value("profile", "name", "")


func _save() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("profile", "name", _player_name)
	cfg.save(CONFIG_PATH)
