extends Node

## Mantiene la instancia activa de [PlayerState] y notifica cambios.
##
## El resto del juego nunca debería mutar `state` directamente: para que las
## señales se disparen siempre se usan los métodos `add_tokens`, `set_era`...

signal tokens_changed(value: float)
signal tokens_per_second_changed(value: float)
signal tokens_per_tap_changed(value: float)
signal qubits_changed(value: int)
signal qubit_multiplier_changed(value: float)
signal era_changed(era: int)
signal boss_progress_changed(progress: float)
signal upgrade_purchased(upgrade_id: StringName)
signal upgrade_levelup(upgrade_id: StringName, new_level: int)
signal state_loaded()

var state: PlayerState = PlayerState.new_default()

## Cuando un minijuego o un evento modal de narrativa toman el control,
## ponen este flag a true para que el ticker pasivo de game.gd se detenga
## hasta que vuelvan a darlo a false al cerrarse.
var passive_paused: bool = false


func reset(keep_qubits: bool = false) -> void:
	var preserved_qubits := state.qubits if keep_qubits else 0
	var preserved_mult := state.qubit_multiplier if keep_qubits else 1.0
	state = PlayerState.new_default()
	state.qubits = preserved_qubits
	state.qubit_multiplier = preserved_mult
	_emit_all()


func load_from(new_state: PlayerState) -> void:
	state = new_state
	_emit_all()
	state_loaded.emit()


func add_tokens(amount: float) -> void:
	if amount == 0.0:
		return
	state.tokens = maxf(0.0, state.tokens + amount)
	if amount > 0.0:
		state.lifetime_tokens += amount
		state.era_lifetime_tokens += amount
	tokens_changed.emit(state.tokens)


func try_spend_tokens(amount: float) -> bool:
	if amount < 0.0 or state.tokens < amount:
		return false
	state.tokens -= amount
	tokens_changed.emit(state.tokens)
	return true


func set_tokens_per_second(value: float) -> void:
	state.tokens_per_second = maxf(0.0, value)
	tokens_per_second_changed.emit(state.tokens_per_second)


func set_tokens_per_tap(value: float) -> void:
	state.tokens_per_tap = maxf(0.0, value)
	tokens_per_tap_changed.emit(state.tokens_per_tap)


func add_qubits(amount: int) -> void:
	if amount == 0:
		return
	state.qubits = maxi(0, state.qubits + amount)
	qubits_changed.emit(state.qubits)


func set_qubit_multiplier(value: float) -> void:
	var clamped := maxf(1.0, value)
	if is_equal_approx(state.qubit_multiplier, clamped):
		return
	state.qubit_multiplier = clamped
	qubit_multiplier_changed.emit(clamped)


func set_era(era: int) -> void:
	if state.current_era == era:
		return
	state.current_era = era
	state.boss_progress = 0.0
	state.era_lifetime_tokens = 0.0
	era_changed.emit(era)
	boss_progress_changed.emit(0.0)


func mark_boss_defeated(era: int) -> void:
	if not state.bosses_defeated.has(era):
		state.bosses_defeated.append(era)


func is_boss_defeated(era: int) -> bool:
	return state.bosses_defeated.has(era)


func set_boss_progress(progress: float) -> void:
	var clamped := clampf(progress, 0.0, 1.0)
	if is_equal_approx(state.boss_progress, clamped):
		return
	state.boss_progress = clamped
	boss_progress_changed.emit(clamped)


func get_upgrade_level(upgrade_id: StringName) -> int:
	return int(state.upgrade_levels.get(String(upgrade_id), 0))


func increment_upgrade_level(upgrade_id: StringName) -> int:
	var old_level := get_upgrade_level(upgrade_id)
	var new_level := old_level + 1
	state.upgrade_levels[String(upgrade_id)] = new_level
	if old_level == 0:
		upgrade_purchased.emit(upgrade_id)
	upgrade_levelup.emit(upgrade_id, new_level)
	return new_level


func record_ethical_decision(event_id: StringName, choice_id: StringName) -> void:
	state.ethical_decisions[String(event_id)] = String(choice_id)


func mark_event_triggered(event_id: StringName) -> void:
	if not state.triggered_events.has(event_id):
		state.triggered_events.append(event_id)


func has_event_triggered(event_id: StringName) -> bool:
	return state.triggered_events.has(event_id)


func _emit_all() -> void:
	tokens_changed.emit(state.tokens)
	tokens_per_second_changed.emit(state.tokens_per_second)
	tokens_per_tap_changed.emit(state.tokens_per_tap)
	qubits_changed.emit(state.qubits)
	qubit_multiplier_changed.emit(state.qubit_multiplier)
	era_changed.emit(state.current_era)
	boss_progress_changed.emit(state.boss_progress)
