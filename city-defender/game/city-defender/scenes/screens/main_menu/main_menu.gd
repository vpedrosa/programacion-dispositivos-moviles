extends Control

@onready var _play_btn: Button = $CenterContainer/VBox/PlayButton
@onready var _scores_btn: Button = $CenterContainer/VBox/HighscoresButton
@onready var _settings_btn: Button = $CenterContainer/VBox/SettingsButton
@onready var _quit_btn: Button = $CenterContainer/VBox/QuitButton
@onready var _title: Label = $CenterContainer/VBox/Title


func _ready() -> void:
	_play_btn.pressed.connect(_on_play_pressed)
	_scores_btn.pressed.connect(_on_highscores_pressed)
	_settings_btn.pressed.connect(_on_settings_pressed)
	_quit_btn.pressed.connect(_on_quit_pressed)
	FalloutStyle.apply(self)
	_title.add_theme_font_size_override("font_size", 80)
	AudioManager.play_music("main-theme")


func _on_play_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/game/game.tscn")


func _on_highscores_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/highscores/highscores.tscn")


func _on_settings_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/settings/settings.tscn")


func _on_quit_pressed() -> void:
	get_tree().quit()
