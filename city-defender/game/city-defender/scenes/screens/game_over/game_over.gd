extends Control

@onready var score_label: Label = $CenterContainer/VBox/ScoreLabel
@onready var sent_label: Label  = $CenterContainer/VBox/SentLabel

var _final_score: int = 0


func _ready() -> void:
	_final_score = GameState.score
	score_label.text = str(_final_score)
	sent_label.visible = false
	AudioManager.stop_music()
	FalloutStyle.apply(self)
	CursorManager.set_menu_cursor()
	_submit_score()


func _submit_score() -> void:
	await FirebaseManager.submit_score(_final_score)
	sent_label.visible = true


func _on_highscores_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/highscores/highscores.tscn")


func _on_retry_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/game/game.tscn")


func _on_menu_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/main_menu/main_menu.tscn")
