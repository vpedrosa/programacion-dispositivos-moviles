extends Control

@onready var score_label: Label = $CenterContainer/VBox/ScoreLabel
@onready var name_container: Control = $CenterContainer/VBox/NameContainer
@onready var name_input: LineEdit = $CenterContainer/VBox/NameContainer/NameInput

var _final_score: int = 0


func _ready() -> void:
	_final_score = GameState.score
	score_label.text = str(_final_score)
	name_container.visible = false
	AudioManager.stop_music()
	FalloutStyle.apply(self)
	CursorManager.set_menu_cursor()
	_check_top_10()


func _check_top_10() -> void:
	var is_top = await FirebaseManager.is_top_10(_final_score)
	name_container.visible = is_top


func _on_submit_pressed() -> void:
	var player_name := name_input.text.strip_edges()
	if player_name.is_empty():
		return
	await FirebaseManager.submit_score(player_name, _final_score)
	get_tree().change_scene_to_file("res://scenes/screens/highscores/highscores.tscn")


func _on_retry_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/game/game.tscn")


func _on_menu_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/main_menu/main_menu.tscn")
