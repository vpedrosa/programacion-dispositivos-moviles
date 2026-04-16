## Overlay de ajustes en partida.
## Envuelve SettingsPanel añadiendo el comportamiento de overlay (visible/oculto, señales).
class_name InGameSettings
extends CanvasLayer

signal opened
signal closed

@onready var _panel: SettingsPanel = $Overlay/CenterContainer/Panel/SettingsPanel


func _ready() -> void:
	visible = false


func open() -> void:
	visible = true
	opened.emit()
	_panel.refresh()


func close() -> void:
	visible = false
	closed.emit()


func _on_resume_pressed() -> void:
	close()


func _on_main_menu_pressed() -> void:
	closed.emit()
	get_tree().paused = false
	get_tree().change_scene_to_file(ScenePaths.MAIN_MENU)
