extends GutTest

var dm: DifficultyManager


func before_each() -> void:
	dm = DifficultyManager.new()
	add_child_autofree(dm)
	dm.reset()


func test_starts_in_early_phase() -> void:
	assert_eq(dm.get_missile_type(), MissileType.NORMAL)


func test_initial_spawn_interval_matches_default() -> void:
	assert_almost_eq(dm.spawn_interval, dm.initial_spawn_interval, 0.001)


func test_initial_missile_speed_matches_default() -> void:
	assert_almost_eq(dm.missile_speed, dm.initial_speed, 0.001)


func test_spawn_interval_reaches_floor_at_ramp_time() -> void:
	dm.elapsed_time = dm.ramp_time
	dm._update_difficulty()
	assert_almost_eq(dm.spawn_interval, dm.min_spawn_interval, 0.001)


func test_missile_speed_reaches_ceiling_at_ramp_time() -> void:
	dm.elapsed_time = dm.ramp_time
	dm._update_difficulty()
	assert_almost_eq(dm.missile_speed, dm.max_speed, 0.001)


func test_phase_transitions_to_mid_after_fast_unlock() -> void:
	dm.elapsed_time = dm.fast_missile_unlock_time + 0.1
	dm._update_phase()
	# En fase MID hay un 60% de NORMAL y 40% de FAST: tras varias muestras
	# debería aparecer al menos un FAST.
	var seen_fast := false
	for i in 200:
		if dm.get_missile_type() == MissileType.FAST:
			seen_fast = true
			break
	assert_true(seen_fast, "debería emitir misiles FAST en fase MID")


func test_phase_transitions_to_late_after_heavy_unlock() -> void:
	dm.elapsed_time = dm.heavy_missile_unlock_time + 0.1
	dm._update_phase()
	var seen_heavy := false
	for i in 500:
		if dm.get_missile_type() == MissileType.HEAVY:
			seen_heavy = true
			break
	assert_true(seen_heavy, "debería emitir misiles HEAVY en fase LATE")


func test_wave_started_emits_at_wave_interval() -> void:
	watch_signals(dm)
	dm._process(dm.wave_interval + 0.01)
	assert_signal_emit_count(dm, "wave_started", 1)


func test_wave_started_does_not_emit_before_first_interval() -> void:
	watch_signals(dm)
	dm._process(dm.wave_interval - 0.5)
	assert_signal_emit_count(dm, "wave_started", 0)


func test_reset_clears_elapsed_time() -> void:
	dm.elapsed_time = 999.0
	dm.reset()
	assert_almost_eq(dm.elapsed_time, 0.0, 0.001)
