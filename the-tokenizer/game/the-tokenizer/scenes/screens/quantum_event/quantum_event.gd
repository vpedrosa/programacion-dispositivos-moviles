extends Control

## Pantalla narrativa del evento cuántico.
##
## Se push_overlay vía game.gd cuando QuantumService emite
## quantum_offered. Muestra cuántos qubits se ganarán al reiniciar,
## cuántos quedarán acumulados tras la operación y el multiplicador
## resultante. La confirmación pasa por un ConfirmationDialog para que
## el reinicio no sea un solo tap.

const GAME_SCENE := "res://scenes/screens/game/game.tscn"

@onready var _qubits_gain_label: Label = %QubitsGainLabel
@onready var _qubits_total_label: Label = %QubitsTotalLabel
@onready var _multiplier_label: Label = %MultiplierLabel
@onready var _confirm_button: Button = %ConfirmButton
@onready var _cancel_button: Button = %CancelButton
@onready var _confirm_modal: ConfirmModal = %ConfirmModal


func _ready() -> void:
	GameState.passive_paused = true
	_confirm_button.pressed.connect(_on_confirm_pressed)
	_cancel_button.pressed.connect(_close)
	_confirm_modal.confirmed.connect(_perform_reset)
	AudioManager.wire_buttons_in(self)
	_refresh()


func _exit_tree() -> void:
	GameState.passive_paused = false


func _refresh() -> void:
	var earned := QuantumService.qubits_on_reset()
	var total: int = GameState.state.qubits + earned
	var multiplier := QuantumService.multiplier_after_reset()
	_qubits_gain_label.text = "+%d qubits" % earned
	_qubits_total_label.text = "Total tras el reset · %d" % total
	_multiplier_label.text = "Multiplicador global · x%.2f" % multiplier


func _on_confirm_pressed() -> void:
	_confirm_modal.set_content(
		"Confirmar reset cuántico",
		"Vas a crear una realidad paralela: tokens, mejoras y decisiones se reinician. Los qubits permanecen.",
		"Reiniciar",
		"Cancelar",
	)
	_confirm_modal.show_modal()


func _perform_reset() -> void:
	# QuantumService.perform_reset() ya limpia el stack de overlays vía
	# SceneManager.clear_overlays(). Antes había aquí un pop_overlay extra
	# porque el reset disparaba como efecto colateral el diálogo de intro
	# de Era 1 (vía era_changed); arreglado en #372 ese push ya no ocurre,
	# así que el pop sobraba.
	QuantumService.perform_reset()
	SceneManager.change_scene(GAME_SCENE)


func _close() -> void:
	SceneManager.pop_overlay()
