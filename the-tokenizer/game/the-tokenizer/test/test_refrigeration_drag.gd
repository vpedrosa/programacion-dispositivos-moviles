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


func test_steady_cadence_wins_within_time_limit() -> void:
	# Con la calibración (#371) el minijuego debe poder resolverse a una
	# cadencia razonable. Simulamos la mecánica de _process en un modelo
	# standalone — replicar `_maybe_shake` exigiría falsificar
	# `Time.get_ticks_msec`, que es el reloj real y por tanto no admite
	# avance determinista en cuestión de milisegundos.
	var refrig: Control = REFRIG_SCENE.instantiate()
	var heat_per_second: float = refrig.HEAT_PER_SECOND
	var cool_per_shake: float = refrig.COOL_PER_SHAKE
	var optimal_min: float = refrig.OPTIMAL_MIN
	var optimal_max: float = refrig.OPTIMAL_MAX
	var target_hold: float = refrig.TARGET_HOLD
	var time_limit: float = refrig.TIME_LIMIT
	refrig.queue_free()
	var delta := 0.05
	var shake_interval := 0.45 # ~2.2 shakes/s, holgado bajo el cooldown de 0.18 s
	var temperature := 50.0
	var hold := 0.0
	var time_left := time_limit
	var time_since_shake := 0.0
	var success := false
	while time_left > 0.0:
		time_left -= delta
		temperature = clampf(temperature + heat_per_second * delta, 0.0, 100.0)
		time_since_shake += delta
		if time_since_shake >= shake_interval:
			time_since_shake = 0.0
			temperature = clampf(temperature - cool_per_shake, 0.0, 100.0)
		if temperature >= optimal_min and temperature <= optimal_max:
			hold += delta
			if hold >= target_hold:
				success = true
				break
		else:
			hold = maxf(0.0, hold - delta * 0.5)
	assert_true(success,
		"A 2.2 shakes/s el minijuego debe completar el hold antes del límite")


func test_single_shake_does_not_dominate_optimal_band() -> void:
	# La franja óptima [OPTIMAL_MIN, OPTIMAL_MAX] debe seguir siendo
	# significativa: una sola sacudida no debe abarcar más de un cuarto
	# del ancho de la zona (criterio de calibración de #371).
	var refrig: Control = REFRIG_SCENE.instantiate()
	var cool: float = refrig.COOL_PER_SHAKE
	var width: float = refrig.OPTIMAL_MAX - refrig.OPTIMAL_MIN
	refrig.queue_free()
	assert_lt(cool, width * 0.25,
		"Una sacudida no debe consumir más del 25% de la franja óptima")


func test_temp_bar_tints_green_inside_optimal_zone() -> void:
	# #374: el feedback visual de "estás dentro de la franja" debe ser
	# inequívoco. Verificamos que `self_modulate` deriva al verde mientras
	# la temperatura permanece en zona.
	var minigame: Control = REFRIG_SCENE.instantiate()
	minigame.set_process(false)
	get_tree().root.add_child(minigame)
	await get_tree().process_frame
	var bar: ProgressBar = minigame.get_node("Margin/VBox/TempBar")
	# Empiezo del tinte neutro y aplico varios ticks de lerp con la
	# bandera in_zone = true; tras ~250 ms el verde debe dominar al rojo.
	bar.self_modulate = minigame.TEMP_BAR_DEFAULT_TINT
	var delta := 0.05
	for _i in range(20):
		minigame._update_temp_bar_tint(true, delta)
	assert_gt(bar.self_modulate.g, bar.self_modulate.r,
		"Dentro de la zona óptima el canal verde debe dominar al rojo")
	minigame.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame


func test_temp_bar_returns_to_default_outside_zone() -> void:
	# Inverso del anterior: partiendo del tinte verde, fuera de la zona
	# `self_modulate` debe converger de nuevo al blanco por defecto sin
	# quedarse "atascado" en verde.
	var minigame: Control = REFRIG_SCENE.instantiate()
	minigame.set_process(false)
	get_tree().root.add_child(minigame)
	await get_tree().process_frame
	var bar: ProgressBar = minigame.get_node("Margin/VBox/TempBar")
	bar.self_modulate = minigame.TEMP_BAR_OPTIMAL_TINT
	var delta := 0.05
	for _i in range(40):
		minigame._update_temp_bar_tint(false, delta)
	# Tras ~2 s de lerp hacia blanco, los canales deben estar cerca del
	# tinte por defecto (≥ 0.95 en r y g).
	assert_gt(bar.self_modulate.r, 0.95,
		"Fuera de zona, el rojo del tinte debe volver al blanco")
	assert_almost_eq(bar.self_modulate.r, bar.self_modulate.g, 0.05,
		"Fuera de zona, los canales R y G deben estar prácticamente igualados")
	minigame.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame
