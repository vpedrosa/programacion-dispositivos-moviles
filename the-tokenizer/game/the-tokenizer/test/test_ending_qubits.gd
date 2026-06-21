extends GutTest

## El resumen del final debe mostrar los qubits que genera la run
## (QuantumService.qubits_on_reset()), no state.qubits: al llegar al final
## por primera vez state.qubits es 0 porque el reset cuántico devuelve a Era 1,
## y mostrar 0 hacía parecer que el dato estaba roto.

const EndingScene := preload("res://scenes/screens/ending/ending.tscn")

var _ending: Control


func before_each() -> void:
	SceneManager.clear_overlays()
	GameState.reset(false)


func after_each() -> void:
	if is_instance_valid(_ending):
		_ending.queue_free()
		_ending = null
	await get_tree().process_frame
	GameState.reset(false)


func test_summary_shows_run_qubits_even_with_zero_banked() -> void:
	# lifetime = 1.000.000 → floor(sqrt(1e6 / 1e4)) = floor(sqrt(100)) = 10.
	GameState.add_tokens(1_000_000.0)
	assert_eq(GameState.state.qubits, 0,
		"Precondición: el jugador no tiene qubits banqueados")
	assert_eq(QuantumService.qubits_on_reset(), 10,
		"Precondición: la run vale 10 qubits")

	_ending = EndingScene.instantiate()
	get_tree().root.add_child(_ending)
	await get_tree().process_frame

	var label: Label = _ending.get_node("%QubitsLabel")
	assert_string_contains(label.text, "10")
	assert_false(label.text.ends_with("· 0"),
		"El final no debe mostrar 0 qubits cuando la run vale más")
