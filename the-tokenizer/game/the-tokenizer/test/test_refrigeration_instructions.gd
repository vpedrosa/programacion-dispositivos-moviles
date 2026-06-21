extends GutTest

## En escritorio el minijuego de refrigeración debe instruir a arrastrar el
## ratón, no a agitar el teléfono. El texto lo fija el código según la
## plataforma; el .tscn ya no hardcodea el texto de móvil.

const RefrigerationScene := preload("res://scenes/minigames/refrigeration/refrigeration.tscn")

var _game: Control


func before_each() -> void:
	SceneManager.clear_overlays()


func after_each() -> void:
	if is_instance_valid(_game):
		_game.queue_free()
		_game = null
	await get_tree().process_frame
	GameState.passive_paused = false


func test_desktop_shows_mouse_instruction() -> void:
	# La suite corre en escritorio, así que OS.has_feature("mobile") es false.
	assert_false(OS.has_feature("mobile"), "Precondición: plataforma de escritorio")

	_game = RefrigerationScene.instantiate()
	get_tree().root.add_child(_game)
	await get_tree().process_frame

	var instructions: Label = _game.get_node("%Instructions")
	assert_string_contains(instructions.text, "Arrastra",
		"En PC la instrucción debe indicar arrastrar el ratón")
	assert_false(instructions.text.contains("teléfono"),
		"En PC no debe filtrarse el texto de móvil ('teléfono')")


## El .tscn no debe traer el texto de móvil hardcodeado como valor por defecto.
func test_scene_default_has_no_phone_text() -> void:
	var packed_text: String = FileAccess.get_file_as_string(
		"res://scenes/minigames/refrigeration/refrigeration.tscn")
	assert_false(packed_text.contains("Agita el teléfono"),
		"El .tscn no debe hardcodear el texto de móvil; lo fija el código")
