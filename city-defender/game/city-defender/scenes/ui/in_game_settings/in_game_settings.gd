## InGameSettings — overlay de ajustes en partida.
## Extiende CanvasLayer, por lo que no puede heredar SettingsPanelBase (Control).
## La lógica de refresco se mantiene aquí; _style_slider está centralizado en FalloutStyle.
extends CanvasLayer

signal opened
signal closed

@onready var _es_btn: Button        = $Overlay/Panel/VBox/LangHBox/ESButton
@onready var _en_btn: Button        = $Overlay/Panel/VBox/LangHBox/ENButton
@onready var _sound_on_btn: Button  = $Overlay/Panel/VBox/SoundHBox/SoundOnButton
@onready var _sound_off_btn: Button = $Overlay/Panel/VBox/SoundHBox/SoundOffButton
@onready var _volume_slider: HSlider = $Overlay/Panel/VBox/VolumeSlider


func _ready() -> void:
	visible = false
	FalloutStyle.style_slider(_volume_slider)


func open() -> void:
	visible = true
	opened.emit()
	_volume_slider.value = SettingsManager.get_volume()
	_refresh_state()


func close() -> void:
	visible = false
	closed.emit()


func _refresh_state() -> void:
	var lang := SettingsManager.get_language()
	_es_btn.modulate       = FalloutStyle.PHOSPHOR_BRIGHT if lang == "es" else FalloutStyle.PHOSPHOR_DIM
	_en_btn.modulate       = FalloutStyle.PHOSPHOR_BRIGHT if lang == "en" else FalloutStyle.PHOSPHOR_DIM
	var snd := SettingsManager.is_sound_enabled()
	_sound_on_btn.modulate  = FalloutStyle.PHOSPHOR_BRIGHT if snd else FalloutStyle.PHOSPHOR_DIM
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


func _on_close_pressed() -> void:
	close()


func _on_main_menu_pressed() -> void:
	closed.emit()
	get_tree().paused = false
	get_tree().change_scene_to_file("res://scenes/screens/main_menu/main_menu.tscn")
