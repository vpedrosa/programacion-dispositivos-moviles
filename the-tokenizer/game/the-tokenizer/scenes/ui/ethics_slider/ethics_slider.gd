class_name EthicsSlider
extends Control

## Indicador visual del ethical_score del jugador.
##
## Pinta una barra horizontal con gradiente rojo → neutro → verde y un
## knob circular que se desliza al recibir [signal GameState.ethical_score_changed].
## El rango se mapea entre los thresholds del ending: el extremo izquierdo
## corresponde a QUESTIONABLE_THRESHOLD y el derecho a RESPONSIBLE_THRESHOLD.

const BAR_HEIGHT := 8.0
const KNOB_RADIUS := 9.0
const TWEEN_DURATION := 0.4

const COLOR_LEFT := Color(0.85, 0.3, 0.3, 1.0)
const COLOR_CENTER := Color(0.55, 0.55, 0.55, 0.9)
const COLOR_RIGHT := Color(0.35, 0.85, 0.4, 1.0)
const COLOR_TRACK_BORDER := Color(0.15, 0.18, 0.15, 0.85)
const COLOR_KNOB_BORDER := Color(0.05, 0.07, 0.05, 0.95)

var _displayed_score: float = 0.0
var _tween: Tween


func _ready() -> void:
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	custom_minimum_size = Vector2(180.0, KNOB_RADIUS * 2.0 + 4.0)
	GameState.ethical_score_changed.connect(_on_score_changed)
	GameState.state_loaded.connect(_on_state_loaded)
	_displayed_score = float(GameState.state.ethical_score)
	queue_redraw()


func _on_state_loaded() -> void:
	if _tween and _tween.is_running():
		_tween.kill()
	_displayed_score = float(GameState.state.ethical_score)
	queue_redraw()


func _on_score_changed(value: int) -> void:
	if _tween and _tween.is_running():
		_tween.kill()
	_tween = create_tween()
	_tween.tween_method(_set_displayed_score, _displayed_score, float(value), TWEEN_DURATION)


func _set_displayed_score(v: float) -> void:
	_displayed_score = v
	queue_redraw()


func _draw() -> void:
	var w := size.x
	var h := size.y
	if w <= 0.0 or h <= 0.0:
		return
	var bar_y := (h - BAR_HEIGHT) * 0.5
	# Gradiente en franjas (Godot 4 no tiene draw_rect con gradiente nativo).
	var steps := 24
	var slice_w := w / float(steps)
	for i in steps:
		var t := float(i) / float(steps - 1)
		var color: Color
		if t < 0.5:
			color = COLOR_LEFT.lerp(COLOR_CENTER, t * 2.0)
		else:
			color = COLOR_CENTER.lerp(COLOR_RIGHT, (t - 0.5) * 2.0)
		draw_rect(Rect2(i * slice_w, bar_y, slice_w + 1.0, BAR_HEIGHT), color)
	draw_rect(Rect2(0.0, bar_y, w, BAR_HEIGHT), COLOR_TRACK_BORDER, false, 1.0)
	# Knob.
	var min_score := float(GameState.QUESTIONABLE_THRESHOLD)
	var max_score := float(GameState.RESPONSIBLE_THRESHOLD)
	var t_score := clampf(remap(_displayed_score, min_score, max_score, 0.0, 1.0), 0.0, 1.0)
	var knob_x := clampf(t_score * w, KNOB_RADIUS, w - KNOB_RADIUS)
	var knob_color: Color
	if _displayed_score < 0.0:
		var mix := clampf(_displayed_score / min_score, 0.0, 1.0)
		knob_color = COLOR_CENTER.lerp(COLOR_LEFT, mix)
	else:
		var mix := clampf(_displayed_score / max_score, 0.0, 1.0)
		knob_color = COLOR_CENTER.lerp(COLOR_RIGHT, mix)
	var knob_center := Vector2(knob_x, h * 0.5)
	draw_circle(knob_center, KNOB_RADIUS, knob_color)
	draw_arc(knob_center, KNOB_RADIUS, 0.0, TAU, 28, COLOR_KNOB_BORDER, 2.0)
