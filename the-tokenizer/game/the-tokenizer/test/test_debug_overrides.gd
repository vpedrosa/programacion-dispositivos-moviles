extends GutTest

## Cubre los overrides de DebugFlags para upgrades concretas (#379).
##
## Con DEBUG inactivo, effect_value_for devuelve el valor base recibido
## (el del .tres). Con DEBUG activo, los IDs presentes en
## DEBUG_OVERRIDES se sustituyen y el resto sigue usando el valor
## base.


func before_each() -> void:
	DebugFlags.set_eval_multiplier_enabled(false)


func after_each() -> void:
	DebugFlags.set_eval_multiplier_enabled(false)


func test_returns_base_when_debug_off() -> void:
	assert_eq(DebugFlags.effect_value_for(&"era_1_gpu", 0.5), 0.5,
		"con DEBUG off, GPU mantiene su valor base")
	assert_eq(DebugFlags.effect_value_for(&"era_7_rsi", 2.0), 2.0,
		"con DEBUG off, RSI mantiene su valor base")
	assert_eq(DebugFlags.effect_value_for(&"era_1_intern", 5.0), 5.0,
		"con DEBUG off, el resto de upgrades mantiene su valor base")


func test_overrides_gpu_and_rsi_when_debug_on() -> void:
	DebugFlags.set_eval_multiplier_enabled(true)
	assert_eq(DebugFlags.effect_value_for(&"era_1_gpu", 0.5), 100.0,
		"con DEBUG on, GPU pasa a +100/s")
	assert_eq(DebugFlags.effect_value_for(&"era_7_rsi", 2.0), 1000.0,
		"con DEBUG on, RSI pasa a x1000/s")


func test_preserves_base_for_non_overridden_ids_when_debug_on() -> void:
	DebugFlags.set_eval_multiplier_enabled(true)
	assert_eq(DebugFlags.effect_value_for(&"era_1_intern", 5.0), 5.0,
		"con DEBUG on, upgrades sin override mantienen el valor base")
	assert_eq(DebugFlags.effect_value_for(&"era_1_serverroom", 1.3), 1.3)


func test_toggling_debug_triggers_recompute() -> void:
	# UpgradeService escucha eval_multiplier_changed y reaplica los
	# efectos derivados. Verificamos el reset clavando un valor
	# arbitrario en tokens_per_second y comprobando que se sobreescribe
	# al togglear (recompute_derived_stats lo arranca desde 0.0).
	GameState.reset()
	GameState.set_tokens_per_second(9999.0)
	DebugFlags.set_eval_multiplier_enabled(true)
	assert_ne(GameState.state.tokens_per_second, 9999.0,
		"togglear DEBUG debe disparar recompute_derived_stats")
	GameState.reset()
