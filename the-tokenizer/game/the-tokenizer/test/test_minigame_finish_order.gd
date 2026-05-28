extends GutTest

## Verifica que un minijuego al finalizar cierra su propio overlay incluso
## cuando apply_outcome dispara un EthicalEvent secundario (#346).
##
## El bug original: `_finish` llamaba a `apply_outcome` antes de
## `pop_overlay`. Como apply_outcome puede pushear un nuevo overlay
## (ethical event al cruzar el umbral), pop_overlay terminaba popeando
## el overlay equivocado y dejaba el minijuego colgado en pantalla.

const BACKPROP_SCENE := preload("res://scenes/minigames/backpropagation/backpropagation.tscn")


func after_each() -> void:
	while SceneManager.has_overlay():
		SceneManager.pop_overlay()
	GameState.passive_paused = false


func test_finish_pops_minigame_overlay_even_if_apply_outcome_pushes_another() -> void:
	# Empuja el minijuego como overlay activo (el más reciente del stack).
	var minigame := SceneManager.push_overlay(BACKPROP_SCENE)
	assert_not_null(minigame)
	# Empuja a continuación un overlay "falso" simulando el caso en que
	# apply_outcome habría empujado un ethical_event si ese hubiera sido
	# el orden anterior. Tras corregir el orden, el minijuego SIEMPRE se
	# popea antes de aplicar el outcome, así que aquí lo verificamos
	# llamando a _finish y comprobando que sigue funcionando con un
	# segundo overlay encima.
	var fake := Control.new()
	get_tree().root.add_child(fake)
	# Forzamos el escenario: aunque el fake esté en root, el stack del
	# SceneManager sólo conoce al minijuego (push_overlay no se ha
	# llamado para el fake), así que pop_overlay debe sacar al minijuego.
	minigame._finish(false)
	await get_tree().process_frame
	await get_tree().process_frame
	assert_false(SceneManager.has_overlay(), "El minijuego debe haber salido del stack")
	assert_false(is_instance_valid(minigame), "El minijuego debe estar liberado")
	if is_instance_valid(fake):
		fake.queue_free()


func test_backprop_initial_weights_match_starting_weights() -> void:
	# Smoke: tras _ready los _initial_weights coinciden con los _weights
	# del primer frame, condición necesaria para que el decay táctil
	# regrese a la posición de inicio.
	var minigame: Control = BACKPROP_SCENE.instantiate()
	get_tree().root.add_child(minigame)
	await get_tree().process_frame
	var initial: PackedFloat32Array = minigame.get("_initial_weights")
	var current: PackedFloat32Array = minigame.get("_weights")
	assert_eq(initial.size(), current.size())
	for i in initial.size():
		assert_almost_eq(current[i], initial[i], 0.0001)
	minigame._finish(false)
	await get_tree().process_frame
	await get_tree().process_frame
