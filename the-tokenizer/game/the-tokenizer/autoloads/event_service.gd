extends Node

## Motor de eventos éticos.
##
## Carga los EthicalEvent de data/events/ en _ready, escucha
## tokens_changed y era_changed para ver si alguno cumple su condición de
## disparo (era == trigger_era AND era_lifetime_tokens >= trigger_threshold),
## y emite ethical_event_triggered(event) con el primero que toque. Cada
## evento se dispara como máximo una vez por partida; la marca persiste
## via GameState.triggered_events.

signal ethical_event_triggered(event: EthicalEvent)

const EVENTS_DIR := "res://data/events"

var _catalog: Array[EthicalEvent] = []


func _ready() -> void:
	_load_catalog()
	GameState.tokens_changed.connect(_check_triggers)
	GameState.era_changed.connect(_on_era_changed)


func _on_era_changed(_era: int) -> void:
	_check_triggers(GameState.state.tokens)


func record_choice(event: EthicalEvent, choice: Dictionary) -> void:
	if event == null or choice.is_empty():
		return
	var choice_id := String(choice.get("id", ""))
	var weight := int(choice.get("weight", 0))
	GameState.record_ethical_decision(event.id, StringName(choice_id), weight)
	_apply_speed_effect(choice)


## Aplica el mismo buff/debuff temporal que los minijuegos según la velocidad
## que implica la opción elegida (analizada en el campo `speed` de cada choice):
## "slow" (la opción ética que ralentiza) → ÷2, "fast" (la opción que acelera)
## → x2. Reusa el slot de multiplicador de minijuego, así que se ve el mismo
## badge y el tap se colorea verde/rojo igual que con un minijuego.
func _apply_speed_effect(choice: Dictionary) -> void:
	match String(choice.get("speed", "")):
		"slow":
			GameState.set_minigame_multiplier(
				MinigameService.FAILURE_MULTIPLIER, MinigameService.MULTIPLIER_DURATION)
		"fast":
			GameState.set_minigame_multiplier(
				MinigameService.SUCCESS_MULTIPLIER, MinigameService.MULTIPLIER_DURATION)


func _check_triggers(_value: float) -> void:
	var era := GameState.state.current_era
	var progress := GameState.state.era_lifetime_tokens
	for event in _catalog:
		if event.trigger_era != era:
			continue
		if GameState.has_event_triggered(event.id):
			continue
		if progress < event.trigger_threshold:
			continue
		GameState.mark_event_triggered(event.id)
		ethical_event_triggered.emit(event)
		return


func _load_catalog() -> void:
	_catalog.clear()
	for resource in ResourceCatalog.load_dir(EVENTS_DIR):
		if resource is EthicalEvent and resource.id != &"":
			_catalog.append(resource)
		else:
			push_warning("EventService: %s no es un EthicalEvent válido" % resource.resource_path)
	_catalog.sort_custom(func(a, b): return a.trigger_threshold < b.trigger_threshold)
