extends GutTest

## Verifica que SceneManager.clear_overlays() popea todos los overlays
## activos (#353), de modo que tras un reset cuántico el stack quede
## limpio y `tap_target` vuelva a responder.

const CONFIRM_MODAL := preload("res://scenes/ui/confirm_modal/confirm_modal.tscn")


func before_each() -> void:
	SceneManager.clear_overlays()


func test_clear_overlays_empties_stack() -> void:
	SceneManager.push_overlay(CONFIRM_MODAL)
	SceneManager.push_overlay(CONFIRM_MODAL)
	SceneManager.push_overlay(CONFIRM_MODAL)
	assert_true(SceneManager.has_overlay())
	SceneManager.clear_overlays()
	assert_false(SceneManager.has_overlay())
	await get_tree().process_frame
	await get_tree().process_frame


func test_clear_overlays_with_empty_stack_is_noop() -> void:
	assert_false(SceneManager.has_overlay())
	SceneManager.clear_overlays()
	assert_false(SceneManager.has_overlay())


func test_game_state_reset_clears_minigame_multiplier() -> void:
	GameState.set_minigame_multiplier(2.0, 60.0)
	assert_almost_eq(GameState.get_minigame_multiplier(), 2.0, 0.001)
	GameState.reset(false)
	assert_almost_eq(GameState.get_minigame_multiplier(), 1.0, 0.001)
