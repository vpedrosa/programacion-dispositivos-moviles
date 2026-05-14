extends Control

## Pantalla de entrada del juego.
##
## Lee [SaveService] para habilitar/deshabilitar el botón Continuar y delega
## la navegación al [SceneManager]. La introducción narrativa, el juego y los
## ajustes son destinos opcionales — si las escenas no existen todavía el
## SceneManager emite un push_error en consola y no rompe la pantalla.

const INTRO_SCENE := "res://scenes/screens/intro/intro.tscn"
const GAME_SCENE := "res://scenes/screens/game/game.tscn"
const SETTINGS_SCENE := "res://scenes/screens/settings/settings.tscn"

@onready var _new_game_button: Button = %NewGameButton
@onready var _continue_button: Button = %ContinueButton
@onready var _settings_button: Button = %SettingsButton
@onready var _quit_button: Button = %QuitButton
@onready var _confirm_dialog: ConfirmationDialog = %ConfirmNewGameDialog


func _ready() -> void:
	_new_game_button.pressed.connect(_on_new_game_pressed)
	_continue_button.pressed.connect(_on_continue_pressed)
	_settings_button.pressed.connect(_on_settings_pressed)
	_quit_button.pressed.connect(_on_quit_pressed)
	_confirm_dialog.confirmed.connect(_start_new_game)
	_continue_button.disabled = not SaveService.has_save()


func _on_new_game_pressed() -> void:
	if SaveService.has_save():
		_confirm_dialog.popup_centered()
	else:
		_start_new_game()


func _on_continue_pressed() -> void:
	var state := SaveService.load_save()
	if state == null:
		push_warning("MainMenu: no se pudo cargar el guardado")
		_continue_button.disabled = true
		return
	GameState.load_from(state)
	SceneManager.change_scene(GAME_SCENE)


func _on_settings_pressed() -> void:
	SceneManager.push_overlay(SETTINGS_SCENE)


func _on_quit_pressed() -> void:
	get_tree().quit()


func _start_new_game() -> void:
	GameState.reset()
	SceneManager.change_scene(INTRO_SCENE)
