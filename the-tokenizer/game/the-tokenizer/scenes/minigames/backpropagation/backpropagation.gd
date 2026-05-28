extends Control

## Minijuego de backpropagation con multitouch.
##
## Cuatro pesos verticales. Cada toque o clic sobre el área de tracks
## se asocia al peso de la columna donde aterriza, y mover el dedo /
## ratón arrastra ese peso entre 0 y 1. La pista soporta varios dedos
## simultáneos (cada touch.index controla una columna distinta) y
## también el ratón en escritorio para validar el ciclo sin un
## dispositivo táctil.
##
## En dispositivos móviles (`OS.has_feature("mobile")` y pantalla táctil)
## los pesos que no estén sostenidos por ningún dedo vuelven progresivamente
## a su posición inicial a razón de [constant TOUCH_DECAY_RATE]: forzar
## a usar varios dedos a la vez es parte del reto. En escritorio el peso
## se queda donde el ratón lo dejó.
##
## El minijuego se resuelve con éxito cuando los cuatro pesos están
## dentro de ±TOLERANCE de su marcador rojo durante HOLD_DURATION
## segundos seguidos antes de que TIME_LIMIT llegue a cero.

const WEIGHTS_COUNT := 4
const HOLD_DURATION := 1.2
const TIME_LIMIT := 18.0
const TOLERANCE := 0.07
const MOUSE_TOUCH_ID := -100
## Velocidad a la que un peso "huérfano" (ningún dedo encima) vuelve a su
## valor inicial en dispositivos táctiles. Unidades: fracción del rango
## [0,1] por segundo.
const TOUCH_DECAY_RATE := 0.4

@onready var _time_label: Label = %TimeLabel
@onready var _hint_label: Label = %HintLabel
@onready var _skip_button: Button = %SkipButton

var _weights: PackedFloat32Array = PackedFloat32Array()
var _initial_weights: PackedFloat32Array = PackedFloat32Array()
var _targets: PackedFloat32Array = PackedFloat32Array()
var _touches: Dictionary = {}
var _hold: float = 0.0
var _time_left: float = TIME_LIMIT
var _finished: bool = false
var _touch_decay: bool = false


func _ready() -> void:
	GameState.passive_paused = true
	_touch_decay = OS.has_feature("mobile") and DisplayServer.is_touchscreen_available()
	_weights.resize(WEIGHTS_COUNT)
	_initial_weights.resize(WEIGHTS_COUNT)
	_targets.resize(WEIGHTS_COUNT)
	for i in WEIGHTS_COUNT:
		var w := randf_range(0.05, 0.95)
		_weights[i] = w
		_initial_weights[i] = w
		_targets[i] = randf_range(0.2, 0.8)
	_skip_button.pressed.connect(_finish.bind(false))
	gui_input.connect(_on_gui_input)
	AudioManager.wire_buttons_in(self)
	queue_redraw()


func _exit_tree() -> void:
	GameState.passive_paused = false


func _process(delta: float) -> void:
	if _finished:
		return
	_time_left -= delta
	if _time_left <= 0.0:
		_finish(false)
		return
	if _touch_decay:
		_apply_decay(delta)
	var all_in := true
	for i in WEIGHTS_COUNT:
		if absf(_weights[i] - _targets[i]) > TOLERANCE:
			all_in = false
			break
	if all_in:
		_hold += delta
		if _hold >= HOLD_DURATION:
			_finish(true)
			return
	else:
		_hold = maxf(0.0, _hold - delta * 0.4)
	_time_label.text = "%.1f s" % maxf(0.0, _time_left)
	if all_in:
		_hint_label.text = "Sostén %.1f s" % maxf(0.0, HOLD_DURATION - _hold)
	else:
		_hint_label.text = "Alinea los nodos con los marcadores rojos"
	queue_redraw()


func _apply_decay(delta: float) -> void:
	# Cada peso vuelve a su posición inicial mientras no haya un dedo activo
	# en su columna. Permite p.ej. sostener un peso con un dedo mientras los
	# otros tres regresan por sí solos.
	var held: Dictionary = {}
	for col in _touches.values():
		if col >= 0:
			held[col] = true
	var step := TOUCH_DECAY_RATE * delta
	for i in WEIGHTS_COUNT:
		if held.has(i):
			continue
		var target := _initial_weights[i]
		var diff := target - _weights[i]
		if absf(diff) <= step:
			_weights[i] = target
		else:
			_weights[i] += signf(diff) * step


func _draw() -> void:
	var rect := _tracks_rect()
	var col_width := rect.size.x / WEIGHTS_COUNT
	for i in WEIGHTS_COUNT:
		var center_x := rect.position.x + col_width * (i + 0.5)
		draw_line(
			Vector2(center_x, rect.position.y),
			Vector2(center_x, rect.position.y + rect.size.y),
			Color(0.3, 0.55, 0.4, 0.7),
			4.0,
		)
		var target_y := rect.position.y + (1.0 - _targets[i]) * rect.size.y
		draw_rect(Rect2(center_x - 36, target_y - 4, 72, 8), Color(0.95, 0.4, 0.4, 0.9))
		var handle_y := rect.position.y + (1.0 - _weights[i]) * rect.size.y
		var distance := absf(_weights[i] - _targets[i])
		var color := Color(0.95, 0.4, 0.4, 1) if distance > TOLERANCE else Color(0.45, 0.85, 0.55, 1)
		draw_circle(Vector2(center_x, handle_y), 26.0, color)
		draw_arc(Vector2(center_x, handle_y), 30.0, 0.0, TAU, 24, Color(0.1, 0.15, 0.1, 0.95), 3.0)


func _on_gui_input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		var touch: InputEventScreenTouch = event
		if touch.pressed:
			_touches[touch.index] = _column_at(touch.position)
		else:
			_touches.erase(touch.index)
	elif event is InputEventScreenDrag:
		var drag: InputEventScreenDrag = event
		if _touches.has(drag.index):
			_drag_column(_touches[drag.index], drag.position)
	elif event is InputEventMouseButton:
		var mb: InputEventMouseButton = event
		if mb.button_index == MOUSE_BUTTON_LEFT:
			if mb.pressed:
				_touches[MOUSE_TOUCH_ID] = _column_at(mb.position)
			else:
				_touches.erase(MOUSE_TOUCH_ID)
	elif event is InputEventMouseMotion:
		if _touches.has(MOUSE_TOUCH_ID):
			_drag_column(_touches[MOUSE_TOUCH_ID], event.position)


func _column_at(local_pos: Vector2) -> int:
	var rect := _tracks_rect()
	if not rect.has_point(local_pos):
		return -1
	var col_width := rect.size.x / WEIGHTS_COUNT
	var idx := int((local_pos.x - rect.position.x) / col_width)
	return clampi(idx, 0, WEIGHTS_COUNT - 1)


func _drag_column(column: int, local_pos: Vector2) -> void:
	if column < 0:
		return
	var rect := _tracks_rect()
	if rect.size.y <= 0.0:
		return
	var t := clampf((local_pos.y - rect.position.y) / rect.size.y, 0.0, 1.0)
	_weights[column] = 1.0 - t


func _tracks_rect() -> Rect2:
	return Rect2(
		Vector2(24, 240),
		Vector2(maxf(80.0, size.x - 48), maxf(80.0, size.y - 240 - 140)),
	)


func _finish(success: bool) -> void:
	if _finished:
		return
	_finished = true
	# Importante: cerrar el overlay ANTES de aplicar el outcome. apply_outcome
	# añade tokens, lo que puede disparar EventService.ethical_event_triggered
	# y empujar un overlay nuevo encima; si llamáramos a pop_overlay después,
	# estaríamos cerrando ese nuevo overlay y dejando el minijuego colgado.
	SceneManager.pop_overlay()
	MinigameService.apply_outcome(success)
