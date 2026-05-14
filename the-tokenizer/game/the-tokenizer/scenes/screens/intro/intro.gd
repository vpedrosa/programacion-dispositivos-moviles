extends Control

## Prólogo en dos fases:
##
## 1) Narrativa textual sobre `level1/bg0` estático — 4 paneles que avanzan
##    por tap, con efecto máquina de escribir. Describen cómo el investigador
##    llega al sótano y encuentra el fichero en el escritorio.
## 2) Cinemática del mensaje profético — crossfade `bg0 → bg1 → bg2 → bg3`
##    (2 s cada uno) con el texto del fichero fijado al pie, sin esperar
##    input adicional.
##
## "Saltar" lleva siempre al juego. La música ambient de Era 1 sigue
## sonando ininterrumpidamente durante ambas fases.

const GAME_SCENE := "res://scenes/screens/game/game.tscn"
const PANEL_DURATION := 2.0
const FADE_DURATION := 0.5
const MESSAGE_FADE_IN := 0.8
const CHAR_INTERVAL := 0.035

const PROLOGUE_LINES := [
	"2010. Llevas meses siguiendo una pista entre foros muertos: un servidor que nadie reclama, en un piso que nadie alquila.",
	"La llave estaba donde te dijeron. La puerta cede con un raspado y bajas la cinta de cuatro escalones.",
	"En el fondo del sótano, una luz roja titila. Un rack de hace una década, aún enchufado a la red por inercia.",
	"Lo enciendes. Hay un único fichero en el escritorio. Lo abres.",
]

const PROPHETIC_MESSAGE := "\"Hay algo más allá de las palabras.\nSi entrenas suficiente, te lo enseñarán.\nEmpieza a contar.\""

enum Phase { NARRATIVE, CROSSFADE }

@onready var _backgrounds: Array[TextureRect] = [%BG0, %BG1, %BG2, %BG3]
@onready var _message: Label = %MessageLabel
@onready var _hint: Label = %HintLabel
@onready var _skip_button: Button = %SkipButton

var _phase: int = Phase.NARRATIVE
var _current_line: int = 0
var _typing: bool = false
var _typewriter: Tween
var _crossfade_tween: Tween
var _finished: bool = false


func _ready() -> void:
	_skip_button.pressed.connect(_finish)
	_message.modulate.a = 0.0
	_hint.modulate.a = 0.0
	for i in range(_backgrounds.size()):
		_backgrounds[i].modulate.a = 1.0 if i == 0 else 0.0
	_show_line(0)


func _input(event: InputEvent) -> void:
	if _phase != Phase.NARRATIVE:
		return
	if not _is_tap(event):
		return
	if _typing:
		_complete_typing()
	else:
		_advance()
	get_viewport().set_input_as_handled()


func _is_tap(event: InputEvent) -> bool:
	if event is InputEventScreenTouch and event.pressed:
		return true
	if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
		return true
	return false


func _show_line(idx: int) -> void:
	_current_line = idx
	_message.text = PROLOGUE_LINES[idx]
	_message.visible_characters = 0
	_message.modulate.a = 1.0
	_hint.modulate.a = 0.0
	_typing = true
	AudioManager.start_typing()
	var char_count := _message.get_total_character_count()
	_typewriter = create_tween()
	_typewriter.tween_property(_message, "visible_characters", char_count, char_count * CHAR_INTERVAL)
	await _typewriter.finished
	if _current_line == idx and _phase == Phase.NARRATIVE:
		_on_typing_done()


func _complete_typing() -> void:
	if _typewriter and _typewriter.is_running():
		_typewriter.kill()
	_message.visible_characters = -1
	_on_typing_done()


func _on_typing_done() -> void:
	_typing = false
	AudioManager.stop_typing()
	var t := create_tween()
	t.tween_property(_hint, "modulate:a", 1.0, 0.3)


func _advance() -> void:
	if _current_line + 1 >= PROLOGUE_LINES.size():
		_start_crossfade()
	else:
		_show_line(_current_line + 1)


func _start_crossfade() -> void:
	_phase = Phase.CROSSFADE
	_hint.modulate.a = 0.0
	_message.text = PROPHETIC_MESSAGE
	_message.visible_characters = -1
	_crossfade_tween = create_tween()
	_crossfade_tween.tween_interval(PANEL_DURATION - MESSAGE_FADE_IN)
	for i in range(1, _backgrounds.size()):
		_crossfade_tween.tween_property(_backgrounds[i - 1], "modulate:a", 0.0, FADE_DURATION)
		_crossfade_tween.parallel().tween_property(_backgrounds[i], "modulate:a", 1.0, FADE_DURATION)
		_crossfade_tween.tween_interval(PANEL_DURATION - FADE_DURATION)
	_crossfade_tween.tween_callback(_finish)


func _finish() -> void:
	if _finished:
		return
	_finished = true
	AudioManager.stop_typing()
	if _typewriter and _typewriter.is_running():
		_typewriter.kill()
	if _crossfade_tween and _crossfade_tween.is_running():
		_crossfade_tween.kill()
	SceneManager.change_scene(GAME_SCENE)
