extends SettingsPanelBase

@onready var _volume_slider: HSlider = $CenterContainer/VBox/VolumeSlider


func _ready() -> void:
	_es_btn       = $CenterContainer/VBox/LangHBox/ESButton
	_en_btn       = $CenterContainer/VBox/LangHBox/ENButton
	_sound_on_btn = $CenterContainer/VBox/SoundHBox/SoundOnButton
	_sound_off_btn = $CenterContainer/VBox/SoundHBox/SoundOffButton
	FalloutStyle.apply(self)
	CursorManager.set_menu_cursor()
	FalloutStyle.style_slider(_volume_slider)
	_volume_slider.value = SettingsManager.get_volume()
	_refresh_state()


func _on_back_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/main_menu/main_menu.tscn")
