extends CanvasLayer

signal shop_requested
signal emp_activated
signal settings_requested

@onready var score_label: Label = $MarginContainer/HBox/ScoreLabel
@onready var money_label: Label = $MarginContainer/HBox/MoneyLabel
@onready var cooldown_bar: ProgressBar = $MarginContainer/HBox/CooldownBar
@onready var emp_button: Button = $MarginContainer/HBox/EMPButton
@onready var city_bars: Array = []


func _ready() -> void:
	GameState.score_changed.connect(_on_score_changed)
	GameState.money_changed.connect(_on_money_changed)
	emp_button.visible = false
	city_bars = []
	for bar in $CityBarsContainer.get_children():
		if bar is ProgressBar:
			city_bars.append(bar)
	FalloutStyle.style_subtree(self)


func _on_score_changed(new_score: int) -> void:
	score_label.text = tr("HUD_SCORE") + str(new_score)


func _on_money_changed(new_money: int) -> void:
	money_label.text = "$" + str(new_money)


func update_cooldown(progress: float) -> void:
	cooldown_bar.value = progress * 100.0


func update_city_health(city_index: int, health: int, max_health: int) -> void:
	if city_index < city_bars.size():
		city_bars[city_index].value = float(health) / float(max_health) * 100.0


func set_emp_available(available: bool) -> void:
	emp_button.visible = available


func show_powerup_active(label: String, remaining: float, total: float) -> void:
	# TODO: show active powerup timer overlay
	pass


func _on_shop_button_pressed() -> void:
	shop_requested.emit()


func _on_emp_button_pressed() -> void:
	emp_activated.emit()


func _on_settings_button_pressed() -> void:
	settings_requested.emit()
