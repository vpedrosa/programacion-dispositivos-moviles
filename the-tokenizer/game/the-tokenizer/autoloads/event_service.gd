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
	if not DirAccess.dir_exists_absolute(EVENTS_DIR):
		return
	var dir := DirAccess.open(EVENTS_DIR)
	if dir == null:
		return
	dir.list_dir_begin()
	var file_name := dir.get_next()
	while file_name != "":
		if not dir.current_is_dir() and file_name.ends_with(".tres"):
			var resource := load(EVENTS_DIR + "/" + file_name)
			if resource is EthicalEvent and resource.id != &"":
				_catalog.append(resource)
			elif resource != null:
				push_warning("EventService: %s no es un EthicalEvent válido" % file_name)
		file_name = dir.get_next()
	_catalog.sort_custom(func(a, b): return a.trigger_threshold < b.trigger_threshold)
