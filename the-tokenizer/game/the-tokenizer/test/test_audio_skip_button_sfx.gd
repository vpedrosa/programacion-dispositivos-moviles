extends GutTest

## Cubre que AudioManager.wire_buttons_in respeta la metadata
## `skip_button_sfx` para no conectar el SFX genérico a botones que
## disparan su propio sonido (caso de las decisiones éticas, #369).

var _root: Control


func before_each() -> void:
	_root = Control.new()
	add_child_autofree(_root)


func test_wires_default_buttons() -> void:
	var button := Button.new()
	_root.add_child(button)
	AudioManager.wire_buttons_in(_root)
	assert_true(button.pressed.is_connected(AudioManager.play_button_sfx),
		"un botón sin metadata debe recibir el SFX genérico")


func test_skips_buttons_marked_with_metadata() -> void:
	var button := Button.new()
	button.set_meta("skip_button_sfx", true)
	_root.add_child(button)
	AudioManager.wire_buttons_in(_root)
	assert_false(button.pressed.is_connected(AudioManager.play_button_sfx),
		"un botón con skip_button_sfx=true no debe recibir el SFX genérico")


func test_metadata_false_still_wires() -> void:
	var button := Button.new()
	button.set_meta("skip_button_sfx", false)
	_root.add_child(button)
	AudioManager.wire_buttons_in(_root)
	assert_true(button.pressed.is_connected(AudioManager.play_button_sfx),
		"skip_button_sfx=false no debe omitir el SFX genérico")
