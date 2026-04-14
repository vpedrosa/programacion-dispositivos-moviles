## Panel de ajustes reutilizable.
## Se usa tanto en la pantalla de ajustes del menú principal como en el overlay en partida.
## El botón "Volver al juego" se muestra u oculta según show_resume_button.
class_name SettingsPanel
extends Control

signal resume_pressed
signal main_menu_pressed

@export var show_resume_button: bool = false

@onready var _es_btn: Button         = $VBox/LangHBox/ESButton
@onready var _en_btn: Button         = $VBox/LangHBox/ENButton
@onready var _sound_on_btn: Button   = $VBox/SoundHBox/SoundOnButton
@onready var _sound_off_btn: Button  = $VBox/SoundHBox/SoundOffButton
@onready var _volume_slider: HSlider = $VBox/VolumeSlider
@onready var _resume_btn: Button     = $VBox/ResumeButton


func _ready() -> void:
	_resume_btn.visible = show_resume_button
	FalloutStyle.style_subtree(self)
	FalloutStyle.style_slider(_volume_slider)
	_volume_slider.value = SettingsManager.get_volume()
	_refresh_state()


## Sincroniza el slider y los botones con el estado actual de SettingsManager.
## Llamar desde el padre al abrir el panel.
func refresh() -> void:
	_volume_slider.value = SettingsManager.get_volume()
	_refresh_state()


func _refresh_state() -> void:
	var lang := SettingsManager.get_language()
	_es_btn.modulate      = FalloutStyle.PHOSPHOR_BRIGHT if lang == "es" else FalloutStyle.PHOSPHOR_DIM
	_en_btn.modulate      = FalloutStyle.PHOSPHOR_BRIGHT if lang == "en" else FalloutStyle.PHOSPHOR_DIM
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


func _on_resume_pressed() -> void:
	resume_pressed.emit()


func _on_main_menu_pressed() -> void:
	main_menu_pressed.emit()
