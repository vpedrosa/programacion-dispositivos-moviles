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
const FONT_BOLD := preload("res://assets/fonts/Silkscreen-Bold.ttf")

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
	_continue_button.disabled = not _any_slot_has_save()
	# Godot 4 Button sólo soporta una fuente, así que el "negrita en hover/press"
	# se hace cambiando la fuente del propio botón con los signals de estado.
	for button in [_new_game_button, _continue_button, _settings_button, _quit_button]:
		button.mouse_entered.connect(_bold.bind(button))
		button.mouse_exited.connect(_regular.bind(button))
		button.button_down.connect(_bold.bind(button))
		button.button_up.connect(_regular.bind(button))


static func _bold(button: Button) -> void:
	button.add_theme_font_override("font", FONT_BOLD)


static func _regular(button: Button) -> void:
	button.remove_theme_font_override("font")


func _on_new_game_pressed() -> void:
	if _any_slot_has_save():
		_confirm_dialog.popup_centered()
	else:
		_start_new_game()


func _on_continue_pressed() -> void:
	var slot := SaveService.get_active_slot()
	if slot == 0:
		slot = _first_occupied_slot()
	var state := SaveService.load_save(slot)
	if state == null:
		push_warning("MainMenu: no se pudo cargar el slot %d" % slot)
		_continue_button.disabled = not _any_slot_has_save()
		return
	SaveService.set_active_slot(slot)
	GameState.load_from(state)
	SceneManager.change_scene(GAME_SCENE)


func _on_settings_pressed() -> void:
	SceneManager.push_overlay(SETTINGS_SCENE)


func _on_quit_pressed() -> void:
	get_tree().quit()


func _start_new_game() -> void:
	# Placeholder: hasta que aterrice el selector de slot en #324 se usa el
	# primer slot vacío disponible (o el 1 si todos están ocupados).
	var target := _first_empty_slot()
	if target == 0:
		target = 1
	SaveService.clear_save(target)
	SaveService.set_active_slot(target)
	GameState.reset()
	SceneManager.change_scene(INTRO_SCENE)


func _any_slot_has_save() -> bool:
	for slot in range(1, SaveService.MAX_SLOTS + 1):
		if SaveService.has_save(slot):
			return true
	return false


func _first_occupied_slot() -> int:
	for slot in range(1, SaveService.MAX_SLOTS + 1):
		if SaveService.has_save(slot):
			return slot
	return 0


func _first_empty_slot() -> int:
	for slot in range(1, SaveService.MAX_SLOTS + 1):
		if not SaveService.has_save(slot):
			return slot
	return 0
