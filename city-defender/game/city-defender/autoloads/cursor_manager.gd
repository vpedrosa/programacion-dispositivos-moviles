## CursorManager — cursores personalizados solo en escritorio.
## Llama a set_menu_cursor() en menús y set_game_cursor() en la escena de juego.
class_name CursorManager
extends Node

const _CLICKER:   Texture2D = preload("res://assets/sprites/mouse/clicker.png")
const _CROSSHAIR: Texture2D = preload("res://assets/sprites/mouse/crosshair.png")

var _desktop: bool = false


func _ready() -> void:
	_desktop = not (OS.has_feature("android") or OS.has_feature("ios") or OS.has_feature("web"))


func set_menu_cursor() -> void:
	if not _desktop:
		return
	Input.set_custom_mouse_cursor(_CLICKER, Input.CURSOR_ARROW, Vector2(0, 0))


func set_game_cursor() -> void:
	if not _desktop:
		return
	# Hotspot en el centro del crosshair (16×16 en una textura 32×32)
	Input.set_custom_mouse_cursor(_CROSSHAIR, Input.CURSOR_ARROW, Vector2(16, 16))
