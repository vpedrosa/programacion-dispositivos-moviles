extends Control

## Pantalla de entrada del juego.
##
## NUEVA y CONTINUAR abren el selector de slot ([url=slot_picker.gd]SlotPicker[/url])
## en modos distintos; el menú no decide sobre qué slot escribir. CONTINUAR se
## deshabilita visualmente si ningún slot tiene partida.

const SETTINGS_SCENE := "res://scenes/screens/settings/settings.tscn"
const SLOT_PICKER_SCENE := preload("res://scenes/screens/slot_picker/slot_picker.tscn")
const FONT_BOLD := preload("res://assets/fonts/Silkscreen-Bold.ttf")

const SlotPicker := preload("res://scenes/screens/slot_picker/slot_picker.gd")

@onready var _new_game_button: Button = %NewGameButton
@onready var _continue_button: Button = %ContinueButton
@onready var _settings_button: Button = %SettingsButton
@onready var _quit_button: Button = %QuitButton


func _ready() -> void:
	_new_game_button.pressed.connect(_on_new_game_pressed)
	_continue_button.pressed.connect(_on_continue_pressed)
	_settings_button.pressed.connect(_on_settings_pressed)
	_quit_button.pressed.connect(_on_quit_pressed)
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
	_open_slot_picker(SlotPicker.Mode.NEW)


func _on_continue_pressed() -> void:
	_open_slot_picker(SlotPicker.Mode.CONTINUE)


func _on_settings_pressed() -> void:
	SceneManager.push_overlay(SETTINGS_SCENE)


func _on_quit_pressed() -> void:
	get_tree().quit()


func _open_slot_picker(mode: int) -> void:
	var picker := SceneManager.push_overlay(SLOT_PICKER_SCENE)
	if picker != null:
		picker.set_mode(mode)


func _any_slot_has_save() -> bool:
	for slot in range(1, SaveService.MAX_SLOTS + 1):
		if SaveService.has_save(slot):
			return true
	return false
