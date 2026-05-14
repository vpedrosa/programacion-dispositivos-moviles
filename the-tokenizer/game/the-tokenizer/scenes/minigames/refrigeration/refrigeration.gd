extends Control

## Minijuego de refrigeración.
##
## El jugador debe agitar el dispositivo a un ritmo razonable para
## mantener la temperatura dentro de [OPTIMAL_MIN, OPTIMAL_MAX]. Sin
## agitar, la temperatura sube; cada shake la baja en COOL_PER_SHAKE.
## Si se mantiene en zona óptima TARGET_HOLD segundos, el minijuego
## se resuelve con éxito. Si el tiempo se agota antes, se reporta
## fallo. SALTAR cancela con fallo y libera el overlay.
##
## En desktop la barra espaciadora simula un shake para poder probar
## el minijuego sin acelerómetro.

const TIME_LIMIT := 15.0
const TARGET_HOLD := 5.0
const TEMP_MIN := 0.0
const TEMP_MAX := 100.0
const OPTIMAL_MIN := 30.0
const OPTIMAL_MAX := 70.0
const HEAT_PER_SECOND := 22.0
const COOL_PER_SHAKE := 9.0
const SHAKE_COOLDOWN := 0.18
const ACCEL_THRESHOLD := 12.0

@onready var _temp_bar: ProgressBar = %TempBar
@onready var _temp_label: Label = %TempLabel
@onready var _time_label: Label = %TimeLabel
@onready var _hint_label: Label = %HintLabel
@onready var _skip_button: Button = %SkipButton

var _temperature: float = 50.0
var _time_left: float = TIME_LIMIT
var _hold: float = 0.0
var _last_shake_msec: int = 0
var _finished: bool = false


func _ready() -> void:
	GameState.passive_paused = true
	_skip_button.pressed.connect(_finish.bind(false))
	_temp_bar.min_value = TEMP_MIN
	_temp_bar.max_value = TEMP_MAX
	_temp_bar.value = _temperature
	AudioManager.wire_buttons_in(self)
	_update_labels()


func _exit_tree() -> void:
	GameState.passive_paused = false


func _process(delta: float) -> void:
	if _finished:
		return
	_time_left -= delta
	if _time_left <= 0.0:
		_finish(false)
		return
	_temperature = clampf(_temperature + HEAT_PER_SECOND * delta, TEMP_MIN, TEMP_MAX)
	if _maybe_shake():
		_temperature = clampf(_temperature - COOL_PER_SHAKE, TEMP_MIN, TEMP_MAX)
	if _temperature >= OPTIMAL_MIN and _temperature <= OPTIMAL_MAX:
		_hold += delta
		if _hold >= TARGET_HOLD:
			_finish(true)
			return
	else:
		_hold = maxf(0.0, _hold - delta * 0.5)
	_update_labels()


func _maybe_shake() -> bool:
	var now := Time.get_ticks_msec()
	if now - _last_shake_msec < int(SHAKE_COOLDOWN * 1000.0):
		return false
	var accel := Input.get_accelerometer()
	if accel.length() >= ACCEL_THRESHOLD:
		_last_shake_msec = now
		return true
	if Input.is_action_just_pressed("ui_accept"):
		_last_shake_msec = now
		return true
	return false


func _update_labels() -> void:
	_temp_bar.value = _temperature
	_temp_label.text = "%d°C" % roundi(_temperature)
	_time_label.text = "%.1f s" % maxf(0.0, _time_left)
	if _temperature < OPTIMAL_MIN:
		_hint_label.text = "Sigue agitando."
	elif _temperature > OPTIMAL_MAX:
		_hint_label.text = "Demasiado calor — sigue."
	else:
		_hint_label.text = "Zona óptima · mantener %.1f s" % maxf(0.0, TARGET_HOLD - _hold)


func _finish(success: bool) -> void:
	if _finished:
		return
	_finished = true
	MinigameService.apply_outcome(success)
	SceneManager.pop_overlay()
