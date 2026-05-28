extends GutTest

## Verifica que el qubit_multiplier se aplica al tap manual y se muestra
## como badge azul persistente al lado del PerSecond (#373).

const GAME_SCENE := preload("res://scenes/screens/game/game.tscn")
const TAP_TARGET_SCENE := preload("res://scenes/entities/tap_target/tap_target.tscn")


func before_each() -> void:
	SceneManager.clear_overlays()
	GameState.reset(false)


func test_tap_multiplies_tokens_per_tap_by_qubit_multiplier() -> void:
	GameState.set_tokens_per_tap(2.0)
	GameState.set_qubit_multiplier(1.5)
	var tap: Control = TAP_TARGET_SCENE.instantiate()
	get_tree().root.add_child(tap)
	await get_tree().process_frame
	var before := GameState.state.tokens
	tap._on_tap()
	var delta := GameState.state.tokens - before
	# 2.0 * 1.5 = 3.0. DebugFlags está deshabilitado por defecto, así que
	# el bonus es 0 y el delta debe ser exactamente el producto.
	assert_almost_eq(delta, 3.0, 0.001,
		"El tap debe sumar tokens_per_tap * qubit_multiplier")
	tap.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame


func test_tap_blocked_when_overlay_active() -> void:
	# Sanity check: el tap sigue silenciado si hay un overlay encima
	# (comportamiento heredado de #353 que debemos preservar al aplicar
	# el multiplicador).
	GameState.set_tokens_per_tap(2.0)
	GameState.set_qubit_multiplier(1.5)
	var tap: Control = TAP_TARGET_SCENE.instantiate()
	get_tree().root.add_child(tap)
	await get_tree().process_frame
	# Simula un overlay activo apilando uno cualquiera.
	SceneManager.push_overlay(preload("res://scenes/ui/confirm_modal/confirm_modal.tscn"))
	var before := GameState.state.tokens
	tap._on_tap()
	assert_almost_eq(GameState.state.tokens, before, 0.001,
		"Con overlay activo el tap no debe sumar")
	SceneManager.clear_overlays()
	tap.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame


func test_qubit_badge_visible_when_multiplier_above_one() -> void:
	GameState.set_qubit_multiplier(1.5)
	var game: Control = GAME_SCENE.instantiate()
	get_tree().root.add_child(game)
	await get_tree().process_frame
	var badge: Label = game.get_node("Margin/VBox/TokensCenter/PerSecondRow/QubitBadge")
	assert_true(badge.visible, "Con qubit_multiplier > 1 el badge azul debe ser visible")
	assert_string_contains(badge.text, "1.5", "El badge muestra el valor del multiplicador")
	game.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame


func test_qubit_badge_hidden_when_multiplier_is_one() -> void:
	GameState.set_qubit_multiplier(1.0)
	var game: Control = GAME_SCENE.instantiate()
	get_tree().root.add_child(game)
	await get_tree().process_frame
	var badge: Label = game.get_node("Margin/VBox/TokensCenter/PerSecondRow/QubitBadge")
	assert_false(badge.visible,
		"Sin qubits acumulados el badge azul debe permanecer oculto")
	game.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame


func test_qubit_badge_updates_on_multiplier_change() -> void:
	var game: Control = GAME_SCENE.instantiate()
	get_tree().root.add_child(game)
	await get_tree().process_frame
	var badge: Label = game.get_node("Margin/VBox/TokensCenter/PerSecondRow/QubitBadge")
	assert_false(badge.visible, "Precondición: badge oculto en estado por defecto")
	GameState.set_qubit_multiplier(2.0)
	await get_tree().process_frame
	assert_true(badge.visible,
		"El badge debe aparecer al disparar qubit_multiplier_changed")
	assert_string_contains(badge.text, "2", "Refleja el nuevo valor del multiplicador")
	game.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame
