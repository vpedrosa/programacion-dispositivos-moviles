extends GutTest

## Verifica que el tap_target manual de Era 1 vuelve a estar presente y
## responsivo tras flujos que pasan por Era 7 y reinician la partida (#372).

const GAME_SCENE := preload("res://scenes/screens/game/game.tscn")
const TAP_TARGET_SCRIPT := preload("res://scenes/entities/tap_target/tap_target.gd")


func before_each() -> void:
	SceneManager.clear_overlays()
	GameState.reset(false)


func _find_tap_target(play_area: Node) -> Node:
	for child in play_area.get_children():
		if child.get_script() == TAP_TARGET_SCRIPT:
			return child
	return null


func test_play_area_has_tap_target_when_starting_in_era_1() -> void:
	var game: Control = GAME_SCENE.instantiate()
	get_tree().root.add_child(game)
	await get_tree().process_frame
	var play_area: Node = game.get_node("Margin/VBox/PlayArea")
	assert_not_null(_find_tap_target(play_area),
		"Era 1 debe instanciar tap_target en _ready")
	game.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame


func test_play_area_has_no_tap_target_in_era_7() -> void:
	GameState.set_era(PlayerState.ERA_SINGULARITY)
	var game: Control = GAME_SCENE.instantiate()
	get_tree().root.add_child(game)
	await get_tree().process_frame
	var play_area: Node = game.get_node("Margin/VBox/PlayArea")
	assert_null(_find_tap_target(play_area),
		"Era 7 no debe tener tap_target (sólo generación pasiva)")
	game.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame


func test_tap_target_returns_after_quantum_reset_from_era_7() -> void:
	# Arranca en Era 7 como si el jugador llevara una partida avanzada.
	GameState.set_era(PlayerState.ERA_SINGULARITY)
	var game: Control = GAME_SCENE.instantiate()
	get_tree().root.add_child(game)
	await get_tree().process_frame
	var play_area: Node = game.get_node("Margin/VBox/PlayArea")
	assert_null(_find_tap_target(play_area),
		"Precondición: arrancando en Era 7 no hay tap_target")
	# Simula el reset cuántico tal y como lo hace QuantumService.
	SceneManager.clear_overlays()
	GameState.reset(false)
	# queue_free de los hijos viejos se procesa al final del frame, pero
	# los nuevos hijos ya están añadidos por _on_era_changed.
	await get_tree().process_frame
	assert_not_null(_find_tap_target(play_area),
		"Tras reset cuántico debe re-aparecer el tap_target en el HUD")
	assert_false(SceneManager.has_overlay(),
		"El stack de overlays debe quedar limpio para que el tap responda")
	game.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame
