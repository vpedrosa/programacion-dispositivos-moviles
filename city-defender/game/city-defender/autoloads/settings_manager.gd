## SettingsManager — autoload que gestiona idioma y sonido.
## Se carga antes que cualquier escena para que las traducciones estén disponibles.
extends Node

const CONFIG_PATH := "user://settings.cfg"
const SUPPORTED_LOCALES: Array[String] = ["es", "en"]

var _language: String = "es"
var _sound_enabled: bool = true


func _ready() -> void:
	_register_translations()
	_load()
	_apply_language()
	_apply_sound()


# ── API pública ────────────────────────────────────────────────────────────────

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


# ── Aplicar ajustes ────────────────────────────────────────────────────────────

func _apply_language() -> void:
	TranslationServer.set_locale(_language)


func _apply_sound() -> void:
	var idx := AudioServer.get_bus_index("Master")
	AudioServer.set_bus_mute(idx, not _sound_enabled)


# ── Persistencia ───────────────────────────────────────────────────────────────

func _load() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(CONFIG_PATH) == OK:
		_language = cfg.get_value("settings", "language", _detect_language())
		_sound_enabled = cfg.get_value("settings", "sound", true)
	else:
		_language = _detect_language()


func _save() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("settings", "language", _language)
	cfg.set_value("settings", "sound", _sound_enabled)
	cfg.save(CONFIG_PATH)


func _detect_language() -> String:
	var locale := OS.get_locale_language()
	return locale if locale in SUPPORTED_LOCALES else "en"


# ── Traducciones ───────────────────────────────────────────────────────────────

func _register_translations() -> void:
	TranslationServer.add_translation(_make_translation("es", {
		# Menú principal
		"MENU_PLAY":        "JUGAR",
		"MENU_HIGHSCORES":  "PUNTUACIONES",
		"MENU_QUIT":        "SALIR",
		"MENU_SETTINGS":    "AJUSTES",
		# HUD
		"HUD_SCORE":        "PUNTOS: ",
		"HUD_SHOP":         "TIENDA",
		"HUD_EMP":          "EMP",
		# Game Over
		"GO_TITLE":         "GAME OVER",
		"GO_TOPTEN":        "TOP 10 - ESCRIBE TU NOMBRE",
		"GO_SUBMIT":        "ENVIAR",
		"GO_RETRY":         "REINTENTAR",
		"GO_MENU":          "MENU",
		# Highscores
		"HS_TITLE":         "PUNTUACIONES",
		"HS_LOADING":       "CARGANDO...",
		"HS_EMPTY":         "SIN PUNTUACIONES REGISTRADAS",
		"HS_BACK":          "VOLVER",
		# Tienda
		"SHOP_TITLE":       "TIENDA",
		"SHOP_CLOSE":       "CERRAR",
		"SHOP_BUY":         "COMPRAR",
		# Ajustes
		"SETTINGS_TITLE":      "AJUSTES",
		"SETTINGS_LANGUAGE":   "IDIOMA",
		"SETTINGS_SOUND":      "SONIDO",
		"SETTINGS_SOUND_ON":   "ACTIVADO",
		"SETTINGS_SOUND_OFF":  "DESACTIVADO",
		"SETTINGS_BACK":       "VOLVER",
		# Power-ups
		"PU_REPAIR_NAME":    "Reparar ciudad",
		"PU_REPAIR_DESC":    "Restaura vida a una ciudad danada",
		"PU_REBUILD_NAME":   "Reconstruir ciudad",
		"PU_REBUILD_DESC":   "Revive ciudad destruida con HP minimo",
		"PU_SHIELD_NAME":    "Escudo temporal",
		"PU_SHIELD_DESC":    "Escudo en todas las ciudades (1 impacto)",
		"PU_RADIUS_NAME":    "Radio explosion+",
		"PU_RADIUS_DESC":    "Aumenta el radio de explosion permanentemente",
		"PU_DOUBLE_NAME":    "Disparo doble",
		"PU_DOUBLE_DESC":    "2 misiles por toque durante 15s",
		"PU_EMP_NAME":       "Bomba EMP",
		"PU_EMP_DESC":       "Destruye todos los misiles en pantalla",
		"PU_COOLDOWN_NAME":  "Cadencia+",
		"PU_COOLDOWN_DESC":  "Reduce el cooldown de disparo permanentemente",
	}))

	TranslationServer.add_translation(_make_translation("en", {
		# Main menu
		"MENU_PLAY":        "PLAY",
		"MENU_HIGHSCORES":  "HIGHSCORES",
		"MENU_QUIT":        "QUIT",
		"MENU_SETTINGS":    "SETTINGS",
		# HUD
		"HUD_SCORE":        "SCORE: ",
		"HUD_SHOP":         "SHOP",
		"HUD_EMP":          "EMP",
		# Game Over
		"GO_TITLE":         "GAME OVER",
		"GO_TOPTEN":        "TOP 10 - ENTER YOUR NAME",
		"GO_SUBMIT":        "SUBMIT",
		"GO_RETRY":         "RETRY",
		"GO_MENU":          "MENU",
		# Highscores
		"HS_TITLE":         "HIGHSCORES",
		"HS_LOADING":       "LOADING...",
		"HS_EMPTY":         "NO SCORES REGISTERED",
		"HS_BACK":          "BACK",
		# Shop
		"SHOP_TITLE":       "SHOP",
		"SHOP_CLOSE":       "CLOSE",
		"SHOP_BUY":         "BUY",
		# Settings
		"SETTINGS_TITLE":      "SETTINGS",
		"SETTINGS_LANGUAGE":   "LANGUAGE",
		"SETTINGS_SOUND":      "SOUND",
		"SETTINGS_SOUND_ON":   "ON",
		"SETTINGS_SOUND_OFF":  "OFF",
		"SETTINGS_BACK":       "BACK",
		# Power-ups
		"PU_REPAIR_NAME":    "Repair city",
		"PU_REPAIR_DESC":    "Restores health to a damaged city",
		"PU_REBUILD_NAME":   "Rebuild city",
		"PU_REBUILD_DESC":   "Revives destroyed city with minimum HP",
		"PU_SHIELD_NAME":    "Temporary shield",
		"PU_SHIELD_DESC":    "Shield on all cities (absorbs 1 hit)",
		"PU_RADIUS_NAME":    "Explosion radius+",
		"PU_RADIUS_DESC":    "Permanently increases explosion radius",
		"PU_DOUBLE_NAME":    "Double shot",
		"PU_DOUBLE_DESC":    "2 missiles per tap for 15s",
		"PU_EMP_NAME":       "EMP bomb",
		"PU_EMP_DESC":       "Destroys all missiles on screen",
		"PU_COOLDOWN_NAME":  "Rate of fire+",
		"PU_COOLDOWN_DESC":  "Permanently reduces firing cooldown",
	}))


func _make_translation(locale: String, messages: Dictionary) -> Translation:
	var t := Translation.new()
	t.locale = locale
	for key: String in messages:
		t.add_message(key, messages[key])
	return t
