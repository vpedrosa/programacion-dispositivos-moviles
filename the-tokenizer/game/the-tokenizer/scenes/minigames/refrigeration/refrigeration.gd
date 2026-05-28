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
## En móvil el shake se detecta con el acelerómetro. En escritorio se
## consigue arrastrando rápido el ratón con clic izquierdo dentro del
## área del minijuego (velocidad ≥ DRAG_VELOCITY_THRESHOLD px/s).
## La acción "ui_accept" sigue funcionando como atajo para tests.

const TIME_LIMIT := 15.0
const TARGET_HOLD := 5.0
const TEMP_MIN := 0.0
const TEMP_MAX := 100.0
const OPTIMAL_MIN := 30.0
const OPTIMAL_MAX := 70.0
const HEAT_PER_SECOND := 22.0
const COOL_PER_SHAKE := 9.0
const SHAKE_COOLDOWN := 0.18
## Magnitud del delta entre lecturas consecutivas del acelerómetro
## (m/s²) que cuenta como sacudida. Trabajar sobre el delta filtra
## la gravedad (que es ~constante) y deja sólo el movimiento real.
const ACCEL_DELTA_THRESHOLD := 6.0
## Velocidad mínima del cursor (px/s) que cuenta como sacudida en escritorio.
const DRAG_VELOCITY_THRESHOLD := 800.0
## Color frío que parpadea sobre la temperatura cuando se detecta una sacudida.
const SHAKE_FLASH_COLOR := Color(0.55, 0.85, 1.0, 1.0)
const SHAKE_FLASH_DURATION := 0.28

@onready var _temp_bar: ProgressBar = %TempBar
@onready var _temp_label: Label = %TempLabel
@onready var _time_label: Label = %TimeLabel
@onready var _hint_label: Label = %HintLabel
@onready var _instructions_label: Label = %Instructions
@onready var _skip_button: Button = %SkipButton

var _temperature: float = 50.0
var _time_left: float = TIME_LIMIT
var _hold: float = 0.0
var _last_shake_msec: int = 0
var _finished: bool = false
var _is_mobile: bool = false
var _drag_shake_pending: bool = false
var _last_accel: Vector3 = Vector3.ZERO
var _accel_seeded: bool = false
var _shake_flash_tween: Tween


func _ready() -> void:
	GameState.passive_paused = true
	_is_mobile = OS.has_feature("mobile")
	_skip_button.pressed.connect(_finish.bind(false))
	_temp_bar.min_value = TEMP_MIN
	_temp_bar.max_value = TEMP_MAX
	_temp_bar.value = _temperature
	gui_input.connect(_on_gui_input)
	if not _is_mobile:
		_instructions_label.text = "Arrastra rápido el ratón con clic izquierdo para enfriar.\nMantén la temperatura en zona óptima durante 5 s."
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
		_flash_shake()
	if _temperature >= OPTIMAL_MIN and _temperature <= OPTIMAL_MAX:
		_hold += delta
		if _hold >= TARGET_HOLD:
			_finish(true)
			return
	else:
		_hold = maxf(0.0, _hold - delta * 0.5)
	_update_labels()


func _on_gui_input(event: InputEvent) -> void:
	if _is_mobile:
		return
	if event is InputEventMouseMotion:
		var motion: InputEventMouseMotion = event
		# `==` tiene mayor precedencia que `&` en GDScript, así que el
		# paréntesis es obligatorio para que la máscara se evalúe primero.
		if (motion.button_mask & MOUSE_BUTTON_MASK_LEFT) == 0:
			return
		if motion.velocity.length() >= DRAG_VELOCITY_THRESHOLD:
			_drag_shake_pending = true


func _maybe_shake() -> bool:
	var now := Time.get_ticks_msec()
	if now - _last_shake_msec < int(SHAKE_COOLDOWN * 1000.0):
		_drag_shake_pending = false
		return false
	# Trabajar sobre el delta de la aceleración descarta la gravedad
	# (que aporta ~9.8 m/s² constantes) y deja sólo el movimiento real.
	# La primera lectura se usa como semilla — el primer frame nunca dispara.
	var accel := Input.get_accelerometer()
	if not _accel_seeded:
		_last_accel = accel
		_accel_seeded = true
	else:
		var jerk := accel - _last_accel
		_last_accel = accel
		if jerk.length() >= ACCEL_DELTA_THRESHOLD:
			_last_shake_msec = now
			return true
	if _drag_shake_pending:
		_drag_shake_pending = false
		_last_shake_msec = now
		return true
	if Input.is_action_just_pressed("ui_accept"):
		_last_shake_msec = now
		return true
	return false


func _flash_shake() -> void:
	if _shake_flash_tween and _shake_flash_tween.is_running():
		_shake_flash_tween.kill()
	_temp_label.modulate = SHAKE_FLASH_COLOR
	_shake_flash_tween = create_tween()
	_shake_flash_tween.tween_property(
		_temp_label, "modulate", Color(1, 1, 1, 1), SHAKE_FLASH_DURATION
	)


func _update_labels() -> void:
	_temp_bar.value = _temperature
	_temp_label.text = "%d°C" % roundi(_temperature)
	_time_label.text = "%.1f s" % maxf(0.0, _time_left)
	var below := "Sigue agitando." if _is_mobile else "Arrastra rápido para refrigerar."
	var above := "Demasiado calor — sigue." if _is_mobile else "Demasiado calor — sigue arrastrando."
	if _temperature < OPTIMAL_MIN:
		_hint_label.text = below
	elif _temperature > OPTIMAL_MAX:
		_hint_label.text = above
	else:
		_hint_label.text = "Zona óptima · mantener %.1f s" % maxf(0.0, TARGET_HOLD - _hold)


func _finish(success: bool) -> void:
	if _finished:
		return
	_finished = true
	# Ver backpropagation.gd:_finish — el orden importa para no popear el
	# overlay equivocado si apply_outcome dispara un ethical_event.
	SceneManager.pop_overlay()
	MinigameService.apply_outcome(success)
