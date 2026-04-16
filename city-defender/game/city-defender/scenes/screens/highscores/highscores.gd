extends Control

@onready var scores_container: VBoxContainer = $MarginContainer/VBox/ScrollContainer/ScoresContainer
@onready var loading_label: Label = $MarginContainer/VBox/LoadingLabel
@onready var error_label: Label = $MarginContainer/VBox/ErrorLabel


func _ready() -> void:
	error_label.visible = false
	FalloutStyle.apply(self)
	CursorManager.set_menu_cursor()
	_load_scores()


func _load_scores() -> void:
	loading_label.visible = true
	var scores: Array[Dictionary] = await FirebaseManager.get_top_scores()
	loading_label.visible = false
	if scores.is_empty():
		error_label.visible = true
		return
	_populate_table(scores)


func _populate_table(scores: Array[Dictionary]) -> void:
	for child in scores_container.get_children():
		child.queue_free()
	for i in scores.size():
		var entry = scores[i]
		var row := _create_row(i + 1, entry.get("score", 0), entry.get("name", ""), entry.get("date", ""))
		scores_container.add_child(row)
		FalloutStyle.style_subtree(row)


func _create_row(pos: int, score: int, player_name: String, date: String) -> HBoxContainer:
	var row := HBoxContainer.new()
	row.add_theme_constant_override("separation", 16)
	var pos_label := Label.new()
	var name_label := Label.new()
	var score_label := Label.new()
	var date_label := Label.new()
	pos_label.text = str(pos) + "."
	pos_label.custom_minimum_size.x = 40
	name_label.text = player_name if not player_name.is_empty() else tr("HS_ANONYMOUS")
	name_label.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	score_label.text = str(score)
	score_label.custom_minimum_size.x = 120
	score_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
	date_label.text = "— " + date if not date.is_empty() else ""
	date_label.custom_minimum_size.x = 110
	date_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
	date_label.add_theme_font_size_override("font_size", 16)
	row.add_child(pos_label)
	row.add_child(name_label)
	row.add_child(score_label)
	row.add_child(date_label)
	return row


func _on_back_pressed() -> void:
	get_tree().change_scene_to_file(ScenePaths.MAIN_MENU)
