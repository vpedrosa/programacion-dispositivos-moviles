class_name GameOverScreen
extends Control

const _AUTO_HIGHSCORES_DELAY: float = 2.0

@onready var score_label: Label   = $CenterContainer/VBox/ScoreLabel
@onready var sent_label: Label    = $CenterContainer/VBox/SentLabel
@onready var name_input: LineEdit = $CenterContainer/VBox/NameInput
@onready var submit_btn: Button   = $CenterContainer/VBox/SubmitButton
@onready var rank_loading_label: Label   = $CenterContainer/VBox/RankLoadingLabel
@onready var personal_rank_label: Label  = $CenterContainer/VBox/PersonalRankLabel
@onready var global_rank_label: Label    = $CenterContainer/VBox/GlobalRankLabel

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

	if PlayerProfile.has_name():
		name_input.visible = false
		submit_btn.visible = false
		await _submit_with_name(PlayerProfile.get_player_name())


func _on_name_changed(new_text: String) -> void:
	var upper := new_text.to_upper()
	if new_text != upper:
		var caret := name_input.caret_column
		name_input.text = upper
		name_input.caret_column = caret
	submit_btn.disabled = upper.strip_edges().is_empty()


func _on_submit_pressed() -> void:
	var player_name := name_input.text.strip_edges()
	if player_name.is_empty():
		return
	name_input.editable = false
	submit_btn.disabled = true
	PlayerProfile.set_player_name(player_name)
	await _submit_with_name(PlayerProfile.get_player_name())


func _submit_with_name(player_name: String) -> void:
	await FirebaseManager.submit_score(_final_score, player_name)
	sent_label.visible = true
	await _show_ranks(player_name)


func _show_ranks(player_name: String) -> void:
	if not FirebaseManager.is_available():
		return
	rank_loading_label.visible = true

	var player_scores: Array[int] = await FirebaseManager.get_player_scores(player_name)
	if not player_scores.is_empty():
		var personal_rank := _compute_personal_rank(player_scores, _final_score)
		personal_rank_label.text = tr("GO_PERSONAL_RANK") % [personal_rank, player_scores.size()]
		personal_rank_label.visible = true

	var global_rank: int = await FirebaseManager.get_global_rank(_final_score)
	if global_rank > 0:
		global_rank_label.text = tr("GO_GLOBAL_RANK") % global_rank
		global_rank_label.visible = true

	rank_loading_label.visible = false

	if global_rank > 0 and global_rank <= FirebaseManager.TOP_COUNT:
		_schedule_auto_highscores()


## Programa la transición automática a Highscores. El Timer es hijo de la
## pantalla, así que se libera (y la transición se cancela) si el jugador
## pulsa Reintentar/Menu antes de que dispare.
func _schedule_auto_highscores() -> void:
	var timer := Timer.new()
	timer.one_shot = true
	timer.wait_time = _AUTO_HIGHSCORES_DELAY
	timer.timeout.connect(_go_to_highscores)
	add_child(timer)
	timer.start()


func _go_to_highscores() -> void:
	if PlayerProfile.has_name():
		HighscoresScreen.pending_highlight_score = _final_score
		HighscoresScreen.pending_highlight_name = PlayerProfile.get_player_name()
	get_tree().change_scene_to_file(ScenePaths.HIGHSCORES)


## Cuenta cuántas puntuaciones del jugador superan estrictamente a `score` y suma 1.
## Empata el ranking en caso de scores iguales (ranking competitivo estándar).
func _compute_personal_rank(scores_desc: Array[int], score: int) -> int:
	var greater := 0
	for s in scores_desc:
		if s > score:
			greater += 1
	return greater + 1


func _on_highscores_pressed() -> void:
	_go_to_highscores()


func _on_retry_pressed() -> void:
	get_tree().change_scene_to_file(ScenePaths.GAME)


func _on_menu_pressed() -> void:
	get_tree().change_scene_to_file(ScenePaths.MAIN_MENU)
