## Clase base compartida por SettingsScreen e InGameSettings.
## Centraliza la lógica de idioma, sonido y refresco visual para evitar duplicación.
class_name SettingsPanelBase
extends Control

## Las subclases deben exponer estos nodos mediante @onready o asignación directa.
var _es_btn: Button
var _en_btn: Button
var _sound_on_btn: Button
var _sound_off_btn: Button


func _refresh_state() -> void:
	var lang := SettingsManager.get_language()
	if _es_btn:
		_es_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if lang == "es" else FalloutStyle.PHOSPHOR_DIM
	if _en_btn:
		_en_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if lang == "en" else FalloutStyle.PHOSPHOR_DIM
	var snd := SettingsManager.is_sound_enabled()
	if _sound_on_btn:
		_sound_on_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if snd else FalloutStyle.PHOSPHOR_DIM
	if _sound_off_btn:
		_sound_off_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if not snd else FalloutStyle.PHOSPHOR_DIM


func _on_es_pressed() -> void:
	SettingsManager.set_language("es")
	_refresh_state()


func _on_en_pressed() -> void:
	SettingsManager.set_language("en")
	_refresh_state()


func _on_sound_on_pressed() -> void:
	SettingsManager.set_sound(true)
	_refresh_state()


func _on_sound_off_pressed() -> void:
	SettingsManager.set_sound(false)
	_refresh_state()


func _on_volume_changed(value: float) -> void:
	SettingsManager.set_volume(value)
