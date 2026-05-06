extends Node

const CONFIG_PATH := "user://settings.cfg"
const SUPPORTED_LOCALES: Array[String] = ["es", "en"]

var _language: String = "es"
var _sound_enabled: bool = true
var _volume: float = 1.0


func _ready() -> void:
	_load()
	_apply_language()
	_apply_sound()
	_apply_volume()


func set_language(locale: String) -> void:
	if locale in SUPPORTED_LOCALES:
		_language = locale
		_apply_language()
		_save()


func set_sound(enabled: bool) -> void:
	_sound_enabled = enabled
	_apply_sound()
	_save()


func get_language() -> String:
	return _language


func is_sound_enabled() -> bool:
	return _sound_enabled


func set_volume(value: float) -> void:
	_volume = clamp(value, 0.0, 1.0)
	_apply_volume()
	_save()


func get_volume() -> float:
	return _volume


func _apply_language() -> void:
	TranslationServer.set_locale(_language)


func _apply_sound() -> void:
	var idx := AudioServer.get_bus_index("Master")
	AudioServer.set_bus_mute(idx, not _sound_enabled)


func _apply_volume() -> void:
	var idx := AudioServer.get_bus_index("Master")
	AudioServer.set_bus_volume_db(idx, linear_to_db(max(_volume, 0.001)))


func _load() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(CONFIG_PATH) == OK:
		_language = cfg.get_value("settings", "language", _detect_language())
		_sound_enabled = cfg.get_value("settings", "sound", true)
		_volume = cfg.get_value("settings", "volume", 1.0)
	else:
		_language = _detect_language()


func _save() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("settings", "language", _language)
	cfg.set_value("settings", "sound", _sound_enabled)
	cfg.set_value("settings", "volume", _volume)
	cfg.save(CONFIG_PATH)


func _detect_language() -> String:
	var locale := OS.get_locale_language()
	return locale if locale in SUPPORTED_LOCALES else "en"
