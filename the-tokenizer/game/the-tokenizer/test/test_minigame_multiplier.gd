extends GutTest

## Verifica el buff/debuff temporal aplicado por MinigameService al
## resolver un minijuego (#348).

const SUCCESS := MinigameService.SUCCESS_MULTIPLIER
const FAILURE := MinigameService.FAILURE_MULTIPLIER


func before_each() -> void:
	GameState.state = PlayerState.new_default()
	GameState.clear_minigame_multiplier()


func test_success_outcome_sets_x2_multiplier() -> void:
	MinigameService.apply_outcome(true)
	assert_almost_eq(GameState.get_minigame_multiplier(), SUCCESS, 0.001)


func test_failure_outcome_sets_half_multiplier() -> void:
	MinigameService.apply_outcome(false)
	assert_almost_eq(GameState.get_minigame_multiplier(), FAILURE, 0.001)


func test_multiplier_expires_after_duration() -> void:
	GameState.set_minigame_multiplier(2.0, 0.0)
	# El timestamp expira inmediatamente; el siguiente get vuelve a 1.0.
	await get_tree().process_frame
	assert_almost_eq(GameState.get_minigame_multiplier(), 1.0, 0.001)


func test_multiplier_emits_signal_when_set_and_when_expired() -> void:
	watch_signals(GameState)
	GameState.set_minigame_multiplier(0.5, 0.0)
	# Forzar expiración perezosa
	await get_tree().process_frame
	GameState.get_minigame_multiplier()
	assert_signal_emit_count(GameState, "minigame_multiplier_changed", 2)


func test_apply_minigame_delta_does_not_touch_lifetime() -> void:
	GameState.state.tokens = 100.0
	GameState.state.lifetime_tokens = 100.0
	GameState.state.era_lifetime_tokens = 100.0
	GameState.apply_minigame_delta(20.0)
	assert_eq(GameState.state.tokens, 120.0)
	assert_eq(GameState.state.lifetime_tokens, 100.0)
	assert_eq(GameState.state.era_lifetime_tokens, 100.0)
	GameState.apply_minigame_delta(-50.0)
	assert_eq(GameState.state.tokens, 70.0)
	assert_eq(GameState.state.lifetime_tokens, 100.0)


func test_clear_minigame_multiplier_resets_to_one() -> void:
	GameState.set_minigame_multiplier(2.0, 60.0)
	GameState.clear_minigame_multiplier()
	assert_almost_eq(GameState.get_minigame_multiplier(), 1.0, 0.001)
	assert_eq(GameState.get_minigame_multiplier_remaining(), 0.0)
