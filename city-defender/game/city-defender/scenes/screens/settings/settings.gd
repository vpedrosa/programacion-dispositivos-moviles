extends Control


func _ready() -> void:
	FalloutStyle.apply(self)
	CursorManager.set_menu_cursor()


func _on_main_menu_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/main_menu/main_menu.tscn")
