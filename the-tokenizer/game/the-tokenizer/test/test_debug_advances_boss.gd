extends GutTest

## Con el modo debug activo, el bonus ×1000 debe contar para era_lifetime_tokens,
## de modo que la barra/cálculo del jefe avancen igual que el balance. Antes el
## bonus iba sólo al gastable, así que se podían acumular millones de tokens sin
## completar la Era 1.

const TapTargetScene := preload("res://scenes/entities/tap_target/tap_target.tscn")

var _tap: Control


func before_each() -> void:
	SceneManager.clear_overlays()
	DebugFlags.set_eval_multiplier_enabled(true)
	GameState.reset(false)
	GameState.clear_minigame_multiplier()


func after_each() -> void:
	if is_instance_valid(_tap):
		_tap.queue_free()
		_tap = null
	await get_tree().process_frame
	# DebugFlags persiste en autoload y en disco: hay que dejarlo apagado.
	DebugFlags.set_eval_multiplier_enabled(false)
	GameState.reset(false)


func _spawn_tap() -> void:
	_tap = TapTargetScene.instantiate()
	get_tree().root.add_child(_tap)
	await get_tree().process_frame


func test_debug_bonus_counts_toward_era_lifetime_and_boss() -> void:
	var base := GameState.state.tokens_per_tap * GameState.state.qubit_multiplier
	var per_tap := base + DebugFlags.bonus_for(base)  # 1 + 999 = 1000
	assert_almost_eq(per_tap, 1000.0, 0.001, "Precondición: el bonus debug es ×1000")
	await _spawn_tap()

	_tap._on_tap()

	assert_almost_eq(GameState.state.era_lifetime_tokens, per_tap, 0.001,
		"Con debug, el bonus debe contar para era_lifetime (no sólo el gastable)")
	var threshold := BossService.get_threshold(GameState.state.current_era)
	assert_almost_eq(GameState.state.boss_progress, per_tap / threshold, 0.001,
		"La barra del jefe debe reflejar el bonus de debug")


func test_lifetime_also_includes_debug_bonus() -> void:
	await _spawn_tap()
	_tap._on_tap()
	# lifetime alimenta qubits y la entrada a Era 7: también debe acelerarse.
	assert_almost_eq(GameState.state.lifetime_tokens, 1000.0, 0.001,
		"lifetime debe incluir el bonus de debug")


func test_no_debug_bonus_when_disabled() -> void:
	DebugFlags.set_eval_multiplier_enabled(false)
	GameState.reset(false)
	await _spawn_tap()

	_tap._on_tap()

	assert_almost_eq(GameState.state.era_lifetime_tokens, 1.0, 0.001,
		"Sin debug, era_lifetime sólo recibe el base del tap")
