class_name CursorManager
extends RefCounted

const _CLICKER:   Texture2D = preload("res://assets/sprites/mouse/clicker.png")
const _CROSSHAIR: Texture2D = preload("res://assets/sprites/mouse/crosshair.png")


static func set_menu_cursor() -> void:
	if not _is_desktop():
		return
	Input.set_custom_mouse_cursor(_CLICKER, Input.CURSOR_ARROW, Vector2(0, 0))


static func set_game_cursor() -> void:
	if not _is_desktop():
		return
	Input.set_custom_mouse_cursor(_CROSSHAIR, Input.CURSOR_ARROW, Vector2(16, 16))


static func _is_desktop() -> bool:
	return not (OS.has_feature("android") or OS.has_feature("ios") or OS.has_feature("web"))
