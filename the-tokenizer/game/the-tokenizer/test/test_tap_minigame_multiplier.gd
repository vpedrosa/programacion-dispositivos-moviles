extends GutTest

## El multiplicador temporal de minijuego (x2 / ÷2) debe aplicarse también al
## tap manual de Era 1, igual que a la generación pasiva. El extra va sólo al
## balance gastable (no a lifetime/era_lifetime) y el "+N" flotante se colorea:
## verde con buff, rojo con debuff, neutro si no hay multiplicador.

const TapTargetScene := preload("res://scenes/entities/tap_target/tap_target.tscn")
const TapTargetScript := preload("res://scenes/entities/tap_target/tap_target.gd")

var _tap: Control


func before_each() -> void:
	SceneManager.clear_overlays()
	GameState.reset(false)
	GameState.clear_minigame_multiplier()
	GameState.set_tokens_per_tap(1.0)


func after_each() -> void:
	if is_instance_valid(_tap):
		_tap.queue_free()
		_tap = null
	await get_tree().process_frame
	GameState.clear_minigame_multiplier()


func _spawn_tap() -> void:
	_tap = TapTargetScene.instantiate()
	get_tree().root.add_child(_tap)
	await get_tree().process_frame


func _last_floating() -> Label:
	var layer: Node = _tap.get_node("%FloatLayer")
	var labels := layer.get_children()
	return labels.back() if not labels.is_empty() else null


func test_buff_multiplier_applies_to_tap() -> void:
	GameState.set_minigame_multiplier(2.0, 30.0)
	await _spawn_tap()
	var tokens_before := GameState.state.tokens
	var lifetime_before := GameState.state.lifetime_tokens

	_tap._on_tap()

	# base = 1 (tokens_per_tap x qubit_mult). Con x2 el jugador recibe 2.
	assert_almost_eq(GameState.state.tokens - tokens_before, 2.0, 0.001,
		"Con x2, el tap debe sumar el doble al balance")
	# Pero lifetime/era_lifetime sólo reciben el base (1), no el extra.
	assert_almost_eq(GameState.state.lifetime_tokens - lifetime_before, 1.0, 0.001,
		"El extra del multiplicador no debe tocar lifetime")


func test_debuff_multiplier_applies_to_tap() -> void:
	GameState.set_minigame_multiplier(0.5, 30.0)
	await _spawn_tap()
	var tokens_before := GameState.state.tokens

	_tap._on_tap()

	assert_almost_eq(GameState.state.tokens - tokens_before, 0.5, 0.001,
		"Con ÷2, el tap debe sumar la mitad al balance")


func test_floating_is_green_on_buff() -> void:
	GameState.set_minigame_multiplier(2.0, 30.0)
	await _spawn_tap()
	_tap._on_tap()
	var label := _last_floating()
	assert_not_null(label, "El tap debe generar un '+N' flotante")
	assert_eq(label.get_theme_color("font_color"), TapTargetScript.FLOAT_COLOR_BUFF,
		"Con x2 el número del tap debe verse en verde")


func test_floating_is_red_on_debuff() -> void:
	GameState.set_minigame_multiplier(0.5, 30.0)
	await _spawn_tap()
	_tap._on_tap()
	var label := _last_floating()
	assert_eq(label.get_theme_color("font_color"), TapTargetScript.FLOAT_COLOR_DEBUFF,
		"Con ÷2 el número del tap debe verse en rojo")


func test_floating_is_neutral_without_multiplier() -> void:
	await _spawn_tap()
	_tap._on_tap()
	var label := _last_floating()
	assert_eq(label.get_theme_color("font_color"), TapTargetScript.FLOAT_COLOR_DEFAULT,
		"Sin multiplicador el número del tap mantiene el color neutro")
