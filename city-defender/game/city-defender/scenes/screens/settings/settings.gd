extends Control

@onready var _es_btn: Button = $CenterContainer/VBox/LangHBox/ESButton
@onready var _en_btn: Button = $CenterContainer/VBox/LangHBox/ENButton
@onready var _sound_on_btn: Button = $CenterContainer/VBox/SoundHBox/SoundOnButton
@onready var _sound_off_btn: Button = $CenterContainer/VBox/SoundHBox/SoundOffButton
@onready var _volume_slider: HSlider = $CenterContainer/VBox/VolumeSlider


func _ready() -> void:
	FalloutStyle.apply(self)
	CursorManager.set_menu_cursor()
	_style_slider(_volume_slider)
	_volume_slider.value = SettingsManager.get_volume()
	_refresh_state()


func _refresh_state() -> void:
	var lang := SettingsManager.get_language()
	_es_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if lang == "es" else FalloutStyle.PHOSPHOR_DIM
	_en_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if lang == "en" else FalloutStyle.PHOSPHOR_DIM
	var snd := SettingsManager.is_sound_enabled()
	_sound_on_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if snd else FalloutStyle.PHOSPHOR_DIM
	_sound_off_btn.modulate = FalloutStyle.PHOSPHOR_BRIGHT if not snd else FalloutStyle.PHOSPHOR_DIM


func _style_slider(slider: HSlider) -> void:
	# Pista de fondo (oscura con borde verde)
	var track := StyleBoxFlat.new()
	track.bg_color = Color(0.0, 0.08, 0.02, 1.0)
	track.border_color = Color(0.0, 0.9, 0.25, 1.0)
	track.set_border_width_all(1)
	track.content_margin_top = 4.0
	track.content_margin_bottom = 4.0
	slider.add_theme_stylebox_override("slider", track)

	# Área rellena (verde fosforescente)
	var fill := StyleBoxFlat.new()
	fill.bg_color = Color(0.0, 0.9, 0.25, 1.0)
	fill.content_margin_top = 4.0
	fill.content_margin_bottom = 4.0
	slider.add_theme_stylebox_override("grabber_area", fill)

	# Grabber: rectángulo estrecho pixel art (10×28, verde con borde oscuro)
	var img: Image = Image.create(10, 28, false, Image.FORMAT_RGBA8)
	img.fill(Color(0.0, 1.0, 0.3, 1.0))
	for x in 10:
		img.set_pixel(x, 0, Color(0.0, 0.4, 0.1, 1.0))
		img.set_pixel(x, 27, Color(0.0, 0.4, 0.1, 1.0))
	for y in 28:
		img.set_pixel(0, y, Color(0.0, 0.4, 0.1, 1.0))
		img.set_pixel(9, y, Color(0.0, 0.4, 0.1, 1.0))
	var grabber: ImageTexture = ImageTexture.create_from_image(img)
	slider.add_theme_icon_override("grabber", grabber)
	slider.add_theme_icon_override("grabber_highlight", grabber)
	slider.add_theme_icon_override("grabber_disabled", grabber)


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


func _on_volume_changed(value: float) -> void:
	SettingsManager.set_volume(value)


func _on_back_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/main_menu/main_menu.tscn")
