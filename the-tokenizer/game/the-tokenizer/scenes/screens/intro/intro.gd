extends Control

## Introducción narrativa por paneles con efecto máquina de escribir.
##
## La secuencia se inicia desde el menú principal cuando se pulsa
## "Nueva partida". El jugador avanza tocando la pantalla (un tap
## completa el panel activo si la escritura aún corre, otro tap salta
## al siguiente panel). El botón "Saltar" lleva directamente al juego.

const GAME_SCENE := "res://scenes/screens/game/game.tscn"
const CHAR_INTERVAL := 0.035

@export var panels: PackedStringArray = PackedStringArray([
	"Sótano. 2010. Olor a polvo y a placa quemada.\nUna luz parpadeante en el rack del fondo.",
	"El servidor lleva años abandonado. Su disco gira con la cadencia de algo que sabe que sigue vivo.",
	"Un único fichero en el escritorio.\nLo abres.",
	"\"Hay algo más allá de las palabras.\nSi entrenas suficiente, te lo enseñarán.\nEmpieza a contar.\"",
])

@onready var _panel_text: RichTextLabel = %PanelText
@onready var _hint_label: Label = %HintLabel
@onready var _skip_button: Button = %SkipButton

var _current: int = 0
var _typing: bool = false
var _typewriter: Tween


func _ready() -> void:
	_skip_button.pressed.connect(_finish)
	_show_panel(0)


func _unhandled_input(event: InputEvent) -> void:
	if not _is_advance_event(event):
		return
	get_viewport().set_input_as_handled()
	if _typing:
		_complete_typewriter()
	else:
		_advance()


func _is_advance_event(event: InputEvent) -> bool:
	if event is InputEventScreenTouch and event.pressed:
		return true
	if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
		return true
	return false


func _show_panel(index: int) -> void:
	if index >= panels.size():
		_finish()
		return
	_current = index
	_panel_text.text = panels[index]
	_panel_text.visible_characters = 0
	_hint_label.modulate.a = 0.0
	_typing = true
	var char_count := _panel_text.get_parsed_text().length()
	_typewriter = create_tween()
	_typewriter.tween_property(
		_panel_text,
		"visible_characters",
		char_count,
		char_count * CHAR_INTERVAL
	)
	await _typewriter.finished
	if _current == index:
		_on_typewriter_done()


func _complete_typewriter() -> void:
	if _typewriter and _typewriter.is_running():
		_typewriter.kill()
	_panel_text.visible_characters = -1
	_on_typewriter_done()


func _on_typewriter_done() -> void:
	_typing = false
	var hint_tween := create_tween()
	hint_tween.tween_property(_hint_label, "modulate:a", 1.0, 0.4)


func _advance() -> void:
	_show_panel(_current + 1)


func _finish() -> void:
	SceneManager.change_scene(GAME_SCENE)
