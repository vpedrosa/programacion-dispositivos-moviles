extends Control

## Pantalla de entrada del juego.
##
## NUEVA abre el selector de slot ([url=slot_picker.gd]SlotPicker[/url]) para
## elegir hueco. CONTINUAR carga directamente el slot con `timestamp` más alto
## (SaveService.most_recent_slot) sin pasar por el selector, y se deshabilita
## si no hay ninguna partida.

const SETTINGS_SCENE := "res://scenes/screens/settings/settings.tscn"
const SLOT_PICKER_SCENE := preload("res://scenes/screens/slot_picker/slot_picker.tscn")
const GAME_SCENE := "res://scenes/screens/game/game.tscn"

@onready var _new_game_button: Button = %NewGameButton
@onready var _continue_button: Button = %ContinueButton
@onready var _settings_button: Button = %SettingsButton
@onready var _quit_button: Button = %QuitButton


func _ready() -> void:
	_new_game_button.pressed.connect(_on_new_game_pressed)
	_continue_button.pressed.connect(_on_continue_pressed)
	_settings_button.pressed.connect(_on_settings_pressed)
	_quit_button.pressed.connect(_on_quit_pressed)
	_continue_button.disabled = SaveService.most_recent_slot() == 0
	AudioManager.play_ambient(PlayerState.ERA_BASEMENT)
	AudioManager.wire_buttons_in(self)


func _on_new_game_pressed() -> void:
	SceneManager.push_overlay(SLOT_PICKER_SCENE)


func _on_continue_pressed() -> void:
	var slot := SaveService.most_recent_slot()
	if slot == 0:
		return
	var state := SaveService.load_save(slot)
	if state == null:
		push_warning("MainMenu: no se pudo cargar el SLOT %d" % slot)
		return
	SaveService.set_active_slot(slot)
	GameState.load_from(state)
	SceneManager.change_scene(GAME_SCENE)


func _on_settings_pressed() -> void:
	SceneManager.push_overlay(SETTINGS_SCENE)


func _on_quit_pressed() -> void:
	get_tree().quit()
