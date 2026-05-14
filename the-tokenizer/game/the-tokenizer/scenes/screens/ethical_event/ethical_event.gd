extends Control

## Modal que presenta un dilema ético al jugador.
##
## Se push_overlay desde game.gd al recibir EventService.ethical_event_triggered.
## Pausa la generación pasiva (passive_paused), renderiza el prompt y un botón
## por opción. Al elegir, registra la decisión, muestra el feedback de la
## opción durante FEEDBACK_HOLD y cierra el overlay.

const FEEDBACK_HOLD := 2.0

@onready var _prompt_label: Label = %PromptLabel
@onready var _choices_box: VBoxContainer = %Choices
@onready var _feedback_label: Label = %FeedbackLabel

var _event: EthicalEvent


func _ready() -> void:
	GameState.passive_paused = true
	_feedback_label.visible = false
	if _event != null:
		_render()
	AudioManager.wire_buttons_in(self)


func _exit_tree() -> void:
	GameState.passive_paused = false


func set_event(event: EthicalEvent) -> void:
	_event = event
	if is_node_ready():
		_render()


func _render() -> void:
	if _event == null:
		return
	_prompt_label.text = _event.prompt
	for child in _choices_box.get_children():
		child.queue_free()
	for choice in _event.choices:
		if not (choice is Dictionary):
			continue
		var dict: Dictionary = choice
		var button := Button.new()
		button.text = String(dict.get("label", ""))
		button.custom_minimum_size = Vector2(0, 72)
		button.add_theme_font_size_override("font_size", 18)
		button.autowrap_mode = TextServer.AUTOWRAP_WORD
		button.pressed.connect(_on_choice_pressed.bind(dict))
		_choices_box.add_child(button)
	AudioManager.wire_buttons_in(_choices_box)


func _on_choice_pressed(choice: Dictionary) -> void:
	EventService.record_choice(_event, choice)
	for child in _choices_box.get_children():
		if child is Button:
			child.disabled = true
	_feedback_label.text = String(choice.get("feedback", ""))
	_feedback_label.visible = true
	_feedback_label.modulate.a = 0.0
	var t := create_tween()
	t.tween_property(_feedback_label, "modulate:a", 1.0, 0.35)
	t.tween_interval(FEEDBACK_HOLD)
	t.tween_property(_feedback_label, "modulate:a", 0.0, 0.3)
	t.tween_callback(SceneManager.pop_overlay)
