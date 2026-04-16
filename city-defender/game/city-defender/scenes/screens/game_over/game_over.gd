class_name GameOverScreen
extends Control

@onready var score_label: Label   = $CenterContainer/VBox/ScoreLabel
@onready var sent_label: Label    = $CenterContainer/VBox/SentLabel
@onready var name_input: LineEdit = $CenterContainer/VBox/NameInput
@onready var submit_btn: Button   = $CenterContainer/VBox/SubmitButton

var _final_score: int = 0


func _ready() -> void:
	_final_score = GameState.score
	score_label.text = str(_final_score)
	sent_label.visible = false
	submit_btn.disabled = true
	name_input.placeholder_text = tr("GO_NAME_PLACEHOLDER")
	AudioManager.stop_music()
	FalloutStyle.apply(self)
	CursorManager.set_menu_cursor()


func _on_name_changed(new_text: String) -> void:
	var upper := new_text.to_upper()
	if new_text != upper:
		name_input.text = upper
		name_input.caret_column = upper.length()
	submit_btn.disabled = upper.strip_edges().is_empty()


func _on_submit_pressed() -> void:
	var player_name := name_input.text.strip_edges()
	if player_name.is_empty():
		return
	name_input.editable = false
	submit_btn.disabled = true
	await FirebaseManager.submit_score(_final_score, player_name)
	sent_label.visible = true


func _on_highscores_pressed() -> void:
	get_tree().change_scene_to_file(ScenePaths.HIGHSCORES)


func _on_retry_pressed() -> void:
	get_tree().change_scene_to_file(ScenePaths.GAME)


func _on_menu_pressed() -> void:
	get_tree().change_scene_to_file(ScenePaths.MAIN_MENU)
