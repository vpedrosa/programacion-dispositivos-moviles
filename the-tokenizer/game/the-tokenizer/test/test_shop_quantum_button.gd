extends GutTest

## La tienda debe ofrecer un punto de reentrada al reinicio cuántico: la oferta
## automática (quantum_offered) es de una sola vez por sesión, así que si el
## jugador la rechaza necesita poder relanzarla desde la tienda. El botón
## aparece cuando QuantumService.is_available() y muestra los qubits que tiene.

const ShopScene := preload("res://scenes/screens/shop/shop.tscn")

var _shop: Control


func before_each() -> void:
	SceneManager.clear_overlays()
	GameState.reset(false)


func after_each() -> void:
	if is_instance_valid(_shop):
		_shop.queue_free()
		_shop = null
	await get_tree().process_frame
	SceneManager.clear_overlays()
	GameState.reset(false)


func _open_shop() -> void:
	_shop = ShopScene.instantiate()
	get_tree().root.add_child(_shop)
	await get_tree().process_frame


## Era 7 con lifetime suficiente: el botón de reinicio está visible.
func test_button_visible_when_quantum_available() -> void:
	GameState.set_era(PlayerState.ERA_SINGULARITY)
	GameState.add_tokens(250000.0)  # lifetime > UNLOCK_THRESHOLD (200000)
	assert_true(QuantumService.is_available(), "Precondición: reinicio disponible")

	await _open_shop()
	assert_true(_shop._qubit_section.visible,
		"La sección de qubits debe verse cuando el reinicio está disponible")
	assert_true(_shop._quantum_button.visible,
		"El botón de reinicio debe verse en Era 7 con lifetime suficiente")


## Era 1 sin qubits: ni sección ni botón.
func test_section_hidden_in_era_1_without_qubits() -> void:
	await _open_shop()
	assert_false(_shop._qubit_section.visible,
		"Sin qubits y sin disponibilidad no debe verse la sección")


## Era 1 con qubits (vuelta tras un reset): se ve el recuento pero no el botón.
func test_count_shown_but_no_button_when_not_available() -> void:
	GameState.add_qubits(7)
	await _open_shop()
	assert_true(_shop._qubit_section.visible,
		"Con qubits acumulados debe verse la sección aunque no haya reinicio")
	assert_false(_shop._quantum_button.visible,
		"Fuera de Era 7 no debe ofrecerse el botón de reinicio")
	assert_string_contains(_shop._qubit_label.text, "7")


## Pulsar el botón abre el overlay del evento cuántico (relanza la oferta).
func test_pressing_button_pushes_quantum_event() -> void:
	GameState.set_era(PlayerState.ERA_SINGULARITY)
	GameState.add_tokens(250000.0)
	await _open_shop()
	assert_false(SceneManager.has_overlay(), "Precondición: sin overlays")

	_shop._on_quantum_pressed()
	assert_true(SceneManager.has_overlay(),
		"Pulsar el botón debe empujar el overlay del evento cuántico")
