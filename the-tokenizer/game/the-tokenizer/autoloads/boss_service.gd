extends Node

## Avance hacia el jefe de la era actual.
##
## Computa state.boss_progress a partir de era_lifetime_tokens / threshold
## de la era. Cuando la barra alcanza 1.0 emite boss_ready(era) una sola
## vez y se queda esperando a que la era cambie (lo que confirma que el
## jefe ha sido superado) para volver a armar el cálculo.

signal boss_ready(era: int)

## Tokens acumulados en la era necesarios para enfrentar al jefe.
const ERA_THRESHOLDS := {
	1: 5000.0,
	7: 1_000_000.0,
}

var _ready_emitted: bool = false


func _ready() -> void:
	GameState.tokens_changed.connect(_recompute)
	GameState.era_changed.connect(_on_era_changed)
	_recompute(GameState.state.tokens)


func get_threshold(era: int) -> float:
	return float(ERA_THRESHOLDS.get(era, 0.0))


func _recompute(_tokens: float) -> void:
	var threshold := get_threshold(GameState.state.current_era)
	if threshold <= 0.0:
		GameState.set_boss_progress(0.0)
		return
	var progress := clampf(GameState.state.era_lifetime_tokens / threshold, 0.0, 1.0)
	GameState.set_boss_progress(progress)
	if progress >= 1.0 and not _ready_emitted and not GameState.is_boss_defeated(GameState.state.current_era):
		_ready_emitted = true
		boss_ready.emit(GameState.state.current_era)


func _on_era_changed(_era: int) -> void:
	_ready_emitted = false
	_recompute(GameState.state.tokens)
