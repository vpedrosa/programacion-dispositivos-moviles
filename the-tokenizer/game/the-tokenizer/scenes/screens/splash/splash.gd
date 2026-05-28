extends Control

## Splash inicial.
##
## Se ejecuta como `run/main_scene` durante 1.5 s y luego hace fade out
## hacia el menú principal. Un tap en cualquier zona lo salta. Usa la
## imagen de fondo del menú principal y el logotipo centrado para
## mantener la continuidad visual entre arranque y home.

const MAIN_MENU_SCENE := "res://scenes/screens/main_menu/main_menu.tscn"
const HOLD_DURATION := 1.5
const FADE_DURATION := 0.4

@onready var _root: CanvasItem = self

var _finishing: bool = false


func _ready() -> void:
	gui_input.connect(_on_input)
	var tween := create_tween()
	tween.tween_interval(HOLD_DURATION)
	tween.tween_callback(_finish)


func _on_input(event: InputEvent) -> void:
	if _finishing:
		return
	if event is InputEventScreenTouch and event.pressed:
		_finish()
	elif event is InputEventMouseButton and event.pressed:
		_finish()


func _finish() -> void:
	if _finishing:
		return
	_finishing = true
	var tween := create_tween()
	tween.tween_property(_root, "modulate:a", 0.0, FADE_DURATION)
	tween.tween_callback(func(): SceneManager.change_scene(MAIN_MENU_SCENE))
