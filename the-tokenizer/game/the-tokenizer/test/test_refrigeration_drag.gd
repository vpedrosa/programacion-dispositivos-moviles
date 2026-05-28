extends GutTest

## Verifica que el handler de gui_input de refrigeración trata correctamente
## el arrastre de ratón con clic izquierdo (#351).

const REFRIG_SCENE := preload("res://scenes/minigames/refrigeration/refrigeration.tscn")


func _make_motion(button_mask: int, velocity: Vector2) -> InputEventMouseMotion:
	var ev := InputEventMouseMotion.new()
	ev.button_mask = button_mask
	ev.velocity = velocity
	return ev


func test_fast_drag_with_left_button_sets_shake_pending() -> void:
	var minigame: Control = REFRIG_SCENE.instantiate()
	get_tree().root.add_child(minigame)
	await get_tree().process_frame
	minigame.set("_is_mobile", false)
	minigame.set("_drag_shake_pending", false)
	var ev := _make_motion(MOUSE_BUTTON_MASK_LEFT, Vector2(1500, 0))
	minigame._on_gui_input(ev)
	assert_true(minigame.get("_drag_shake_pending"))
	minigame._finish(false)
	await get_tree().process_frame
	await get_tree().process_frame


func test_motion_without_left_button_is_ignored() -> void:
	var minigame: Control = REFRIG_SCENE.instantiate()
	get_tree().root.add_child(minigame)
	await get_tree().process_frame
	minigame.set("_is_mobile", false)
	minigame.set("_drag_shake_pending", false)
	var ev := _make_motion(0, Vector2(1500, 0))
	minigame._on_gui_input(ev)
	assert_false(minigame.get("_drag_shake_pending"),
		"Sin botón pulsado no se cuenta como shake")
	minigame._finish(false)
	await get_tree().process_frame
	await get_tree().process_frame


func test_slow_drag_with_left_button_is_ignored() -> void:
	var minigame: Control = REFRIG_SCENE.instantiate()
	get_tree().root.add_child(minigame)
	await get_tree().process_frame
	minigame.set("_is_mobile", false)
	minigame.set("_drag_shake_pending", false)
	var ev := _make_motion(MOUSE_BUTTON_MASK_LEFT, Vector2(100, 0))
	minigame._on_gui_input(ev)
	assert_false(minigame.get("_drag_shake_pending"),
		"Velocidad por debajo del umbral no cuenta como shake")
	minigame._finish(false)
	await get_tree().process_frame
	await get_tree().process_frame
