extends Node

## Activador y resolución de la realidad cuántica (New Game+).
##
## Desbloquea el evento cuántico cuando la partida cumple
## UNLOCK_ERA (Era 7) y el lifetime_tokens supera UNLOCK_THRESHOLD.
## A partir de ahí, [signal quantum_offered] avisa al HUD para abrir
## la pantalla narrativa. [method perform_reset] calcula los qubits
## ganados con la fórmula floor(sqrt(lifetime_tokens / QUBIT_DIVISOR))
## y actualiza el multiplicador global como 1 + qubits * QUBIT_BONUS.

signal quantum_offered()

const UNLOCK_ERA := 7
const UNLOCK_THRESHOLD := 200000.0
const QUBIT_DIVISOR := 10000.0
const QUBIT_BONUS := 0.05

var _offered_this_session: bool = false


func _ready() -> void:
	GameState.tokens_changed.connect(_on_tokens_changed)


func is_available() -> bool:
	return GameState.state.current_era == UNLOCK_ERA \
		and GameState.state.lifetime_tokens >= UNLOCK_THRESHOLD


func qubits_on_reset() -> int:
	var raw := sqrt(maxf(0.0, GameState.state.lifetime_tokens) / QUBIT_DIVISOR)
	return int(floor(raw))


func multiplier_after_reset() -> float:
	var earned := qubits_on_reset()
	var total_qubits := GameState.state.qubits + earned
	return 1.0 + float(total_qubits) * QUBIT_BONUS


func perform_reset() -> void:
	var earned := qubits_on_reset()
	var preserved := GameState.state.qubits + earned
	# Limpia cualquier overlay activo antes de reiniciar para evitar que un
	# residuo del stack (ej. el propio quantum_event o un minijuego que se
	# disparó antes del confirm) bloquee inputs de la nueva partida vía
	# `SceneManager.has_overlay()`.
	SceneManager.clear_overlays()
	GameState.reset(false)
	GameState.add_qubits(preserved)
	GameState.set_qubit_multiplier(1.0 + float(preserved) * QUBIT_BONUS)
	_offered_this_session = false


func _on_tokens_changed(_value: float) -> void:
	if _offered_this_session:
		return
	if not is_available():
		return
	_offered_this_session = true
	quantum_offered.emit()
