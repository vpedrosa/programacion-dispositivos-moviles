extends GutTest

## Al resolver una decisión ética, la opción elegida aplica el mismo
## multiplicador temporal que los minijuegos según su campo `speed`:
## "slow" (ética que ralentiza) → ÷2, "fast" (acelera) → x2, "none" → nada.

const EVENTS_DIR := "res://data/events"


func before_each() -> void:
	GameState.reset(false)
	GameState.clear_minigame_multiplier()


func after_each() -> void:
	GameState.clear_minigame_multiplier()


func _event_with(choice: Dictionary) -> EthicalEvent:
	var event := EthicalEvent.new()
	event.id = &"test_event"
	event.choices = [choice]
	return event


func test_slow_choice_applies_half_multiplier() -> void:
	var event := _event_with({"id": "x", "weight": 1, "speed": "slow"})
	EventService.record_choice(event, event.choices[0])
	assert_almost_eq(GameState.get_minigame_multiplier(), MinigameService.FAILURE_MULTIPLIER, 0.001,
		"Una opción 'slow' debe aplicar el divisor ÷2 de los minijuegos")


func test_fast_choice_applies_double_multiplier() -> void:
	var event := _event_with({"id": "x", "weight": -1, "speed": "fast"})
	EventService.record_choice(event, event.choices[0])
	assert_almost_eq(GameState.get_minigame_multiplier(), MinigameService.SUCCESS_MULTIPLIER, 0.001,
		"Una opción 'fast' debe aplicar el multiplicador x2 de los minijuegos")


func test_none_choice_leaves_multiplier_untouched() -> void:
	var event := _event_with({"id": "x", "weight": 0, "speed": "none"})
	EventService.record_choice(event, event.choices[0])
	assert_almost_eq(GameState.get_minigame_multiplier(), 1.0, 0.001,
		"Una opción 'none' no debe tocar el multiplicador")


func test_missing_speed_leaves_multiplier_untouched() -> void:
	var event := _event_with({"id": "x", "weight": 0})
	EventService.record_choice(event, event.choices[0])
	assert_almost_eq(GameState.get_minigame_multiplier(), 1.0, 0.001,
		"Sin campo speed no debe tocar el multiplicador")


## Integridad de datos: toda opción del catálogo declara un speed válido y,
## donde el peso marca una postura clara, la velocidad es coherente
## (ética/responsable → slow, cuestionable → fast).
func test_catalog_choices_have_consistent_speed() -> void:
	var dir := DirAccess.open(EVENTS_DIR)
	assert_not_null(dir, "data/events debe existir")
	var any := false
	for file in dir.get_files():
		if not file.ends_with(".tres") and not file.ends_with(".tres.remap"):
			continue
		var path := "%s/%s" % [EVENTS_DIR, file.replace(".remap", "")]
		var event: EthicalEvent = load(path)
		if event == null:
			continue
		for choice in event.choices:
			any = true
			var speed := String(choice.get("speed", ""))
			assert_true(speed in ["slow", "fast", "none"],
				"%s · %s: speed debe ser slow/fast/none (era '%s')" % [
					file, choice.get("id", "?"), speed])
			var weight := int(choice.get("weight", 0))
			if weight > 0:
				assert_eq(speed, "slow",
					"%s · %s: una opción responsable debe ralentizar" % [file, choice.get("id", "?")])
			elif weight < 0:
				assert_eq(speed, "fast",
					"%s · %s: una opción cuestionable debe acelerar" % [file, choice.get("id", "?")])
	assert_true(any, "Debe haberse evaluado al menos una opción del catálogo")
