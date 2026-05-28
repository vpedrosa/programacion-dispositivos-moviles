extends Control

## Pantalla de entrada del juego.
##
## PlayButton abre el selector de slot ([url=slot_picker.gd]SlotPicker[/url]):
## el selector decide si el slot elegido es una partida nueva o cargada. El
## texto del botón cambia entre "Nueva partida" y "Continuar" según haya o
## no partidas guardadas.

const SETTINGS_SCENE := "res://scenes/screens/settings/settings.tscn"
const SLOT_PICKER_SCENE := preload("res://scenes/screens/slot_picker/slot_picker.tscn")

@onready var _play_button: Button = %PlayButton
@onready var _settings_button: Button = %SettingsButton
@onready var _quit_button: Button = %QuitButton


func _ready() -> void:
	_play_button.pressed.connect(_on_play_pressed)
	_settings_button.pressed.connect(_on_settings_pressed)
	_quit_button.pressed.connect(_on_quit_pressed)
	SceneManager.overlay_popped.connect(_on_overlay_popped)
	_refresh_play_label()
	AudioManager.play_ambient(PlayerState.ERA_BASEMENT)
	AudioManager.wire_buttons_in(self)


func _on_overlay_popped(_node: Node) -> void:
	_refresh_play_label()


func _on_play_pressed() -> void:
	SceneManager.push_overlay(SLOT_PICKER_SCENE)


func _on_settings_pressed() -> void:
	SceneManager.push_overlay(SETTINGS_SCENE)


func _on_quit_pressed() -> void:
	get_tree().quit()


func _refresh_play_label() -> void:
	_play_button.text = "Continuar" if _any_slot_has_save() else "Nueva partida"


func _any_slot_has_save() -> bool:
	for slot in range(1, SaveService.MAX_SLOTS + 1):
		if SaveService.has_save(slot):
			return true
	return false
