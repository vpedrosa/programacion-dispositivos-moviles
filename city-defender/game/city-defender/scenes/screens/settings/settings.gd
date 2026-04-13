extends Control

@onready var _es_btn: Button = $CenterContainer/VBox/LangHBox/ESButton
@onready var _en_btn: Button = $CenterContainer/VBox/LangHBox/ENButton
@onready var _sound_on_btn: Button = $CenterContainer/VBox/SoundHBox/SoundOnButton
@onready var _sound_off_btn: Button = $CenterContainer/VBox/SoundHBox/SoundOffButton


func _ready() -> void:
	FalloutStyle.apply(self)
	CursorManager.set_menu_cursor()
	_refresh_state()


func _refresh_state() -> void:
	var lang := SettingsManager.get_language()
	_es_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if lang == "es" else FalloutStyle.PHOSPHOR_DIM
	_en_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if lang == "en" else FalloutStyle.PHOSPHOR_DIM
	var snd := SettingsManager.is_sound_enabled()
	_sound_on_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if snd else FalloutStyle.PHOSPHOR_DIM
	_sound_off_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if not snd else FalloutStyle.PHOSPHOR_DIM


func _on_es_pressed() -> void:
	SettingsManager.set_language("es")
	_refresh_state()


func _on_en_pressed() -> void:
	SettingsManager.set_language("en")
	_refresh_state()


func _on_sound_on_pressed() -> void:
	SettingsManager.set_sound(true)
	_refresh_state()


func _on_sound_off_pressed() -> void:
	SettingsManager.set_sound(false)
	_refresh_state()


func _on_back_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/main_menu/main_menu.tscn")
