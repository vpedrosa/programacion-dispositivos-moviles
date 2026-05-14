extends Control

## Overlay de diálogo con portrait animado.
##
## Recibe un array de líneas y las renderiza una a una con efecto
## máquina de escribir mientras cicla los frames de "researcher
## talking". Un tap durante la escritura completa la línea; otro tap
## avanza. Al cerrar, suelta el flag passive_paused.

signal closed()

const TALKING_FRAMES := [
	preload("res://assets/sprites/player/researcher-talking-animation-1.png"),
	preload("res://assets/sprites/player/researcher-talking-animation-2.png"),
	preload("res://assets/sprites/player/researcher-talking-animation-3.png"),
	preload("res://assets/sprites/player/researcher-talking-idle.png"),
]
const IDLE_FRAME := preload("res://assets/sprites/player/researcher-idle.png")
const CHAR_INTERVAL := 0.035
const TALK_FRAME_INTERVAL := 0.13

@export var lines: PackedStringArray = PackedStringArray()

@onready var _portrait: TextureRect = %Portrait
@onready var _text: RichTextLabel = %TextLabel
@onready var _hint: Label = %Hint
@onready var _talk_timer: Timer = %TalkTimer

var _current_line: int = 0
var _typing: bool = false
var _typewriter: Tween
var _talk_index: int = 0
var _resumed_passive: bool = false


func _ready() -> void:
	GameState.passive_paused = true
	_portrait.texture = IDLE_FRAME
	_talk_timer.timeout.connect(_advance_talk_frame)
	if not lines.is_empty():
		_show_line(0)


func _exit_tree() -> void:
	if not _resumed_passive:
		GameState.passive_paused = false
		_resumed_passive = true


func _unhandled_input(event: InputEvent) -> void:
	if not _is_tap_event(event):
		return
	get_viewport().set_input_as_handled()
	if _typing:
		_complete_typing()
	else:
		_advance()


func set_lines(value: PackedStringArray) -> void:
	lines = value
	if is_node_ready():
		_current_line = 0
		_show_line(0)


func _is_tap_event(event: InputEvent) -> bool:
	if event is InputEventScreenTouch and event.pressed:
		return true
	if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
		return true
	return false


func _show_line(idx: int) -> void:
	if idx >= lines.size():
		_close()
		return
	_current_line = idx
	_text.text = lines[idx]
	_text.visible_characters = 0
	_hint.modulate.a = 0.0
	_typing = true
	_talk_timer.start()
	var char_count := _text.get_parsed_text().length()
	_typewriter = create_tween()
	_typewriter.tween_property(_text, "visible_characters", char_count, char_count * CHAR_INTERVAL)
	await _typewriter.finished
	if _current_line == idx:
		_on_typing_done()


func _complete_typing() -> void:
	if _typewriter and _typewriter.is_running():
		_typewriter.kill()
	_text.visible_characters = -1
	_on_typing_done()


func _on_typing_done() -> void:
	_typing = false
	_talk_timer.stop()
	_portrait.texture = IDLE_FRAME
	var t := create_tween()
	t.tween_property(_hint, "modulate:a", 1.0, 0.3)


func _advance() -> void:
	_show_line(_current_line + 1)


func _advance_talk_frame() -> void:
	_talk_index = (_talk_index + 1) % TALKING_FRAMES.size()
	_portrait.texture = TALKING_FRAMES[_talk_index]


func _close() -> void:
	closed.emit()
	SceneManager.pop_overlay()
