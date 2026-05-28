extends GutTest

## Verifica que record_ethical_decision emite ethical_score_changed
## con el nuevo total (#345).

func before_each() -> void:
	GameState.state = PlayerState.new_default()


func test_signal_emitted_with_new_total_on_positive_weight() -> void:
	watch_signals(GameState)
	GameState.record_ethical_decision(&"era_1_dataset", &"filter_hard", 1)
	assert_signal_emitted_with_parameters(GameState, "ethical_score_changed", [1])


func test_signal_emitted_with_accumulated_total() -> void:
	watch_signals(GameState)
	GameState.record_ethical_decision(&"era_1_dataset", &"filter_hard", 1)
	GameState.record_ethical_decision(&"era_7_evals", &"delay_publish", 2)
	# La última emisión debe llevar el acumulado.
	assert_signal_emit_count(GameState, "ethical_score_changed", 2)
	assert_signal_emitted_with_parameters(GameState, "ethical_score_changed", [3], 1)


func test_signal_emitted_with_negative_total() -> void:
	watch_signals(GameState)
	GameState.record_ethical_decision(&"era_1_dataset", &"leave_raw", -1)
	GameState.record_ethical_decision(&"era_7_compute", &"buy_carbon", -1)
	assert_signal_emitted_with_parameters(GameState, "ethical_score_changed", [-2], 1)


func test_signal_still_emits_on_zero_weight() -> void:
	watch_signals(GameState)
	GameState.record_ethical_decision(&"neutral", &"meh", 0)
	assert_signal_emitted_with_parameters(GameState, "ethical_score_changed", [0])
