extends GutTest

## Smoke test: las escenas críticas cargan e instancian sin errores de parseo.

const SCENES := [
	"res://scenes/screens/game/game.tscn",
	"res://scenes/screens/slot_picker/slot_picker.tscn",
	"res://scenes/screens/quantum_event/quantum_event.tscn",
	"res://scenes/screens/ending/ending.tscn",
	"res://scenes/screens/intro_era7/intro_era7.tscn",
	"res://scenes/ui/confirm_modal/confirm_modal.tscn",
	"res://scenes/minigames/backpropagation/backpropagation.tscn",
	"res://scenes/minigames/refrigeration/refrigeration.tscn",
]


func test_critical_scenes_load() -> void:
	for path in SCENES:
		var scene: PackedScene = load(path)
		assert_not_null(scene, "Scene failed to load: %s" % path)
		if scene == null:
			continue
		var instance := scene.instantiate()
		assert_not_null(instance, "Scene failed to instantiate: %s" % path)
		instance.queue_free()
	# Los warnings de UID stale en sprites del researcher son arrastre
	# pre-existente del proyecto (los .tscn referencian un UID antiguo
	# pero Godot resuelve por path). No bloquean la carga.
	for err in get_errors():
		if err.contains_text("invalid UID"):
			err.handled = true
