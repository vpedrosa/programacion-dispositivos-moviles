extends Node

## Activador aleatorio de minijuegos.
##
## Cada CHECK_INTERVAL segundos (con jitter) evalúa si toca lanzar un
## minijuego: comprueba cooldown, ausencia de overlays modales (tienda,
## ajustes, eventos éticos, boss, intro de era) y un dado de probabilidad.
## Al disparar emite minigame_offered(scene_path) para que la pantalla
## de juego lo recoja, anuncie por toast y haga push_overlay del minijuego.
## Al completarse, apply_outcome decide recompensa o penalización.

signal minigame_offered(scene_path: String)
signal minigame_outcome_applied(success: bool, bonus: float)

const MINIGAME_SCENES := [
	"res://scenes/minigames/backpropagation/backpropagation.tscn",
	"res://scenes/minigames/refrigeration/refrigeration.tscn",
]

const CHECK_INTERVAL := 50.0
const CHECK_JITTER := 30.0
const COOLDOWN := 90.0
const TRIGGER_CHANCE := 0.4
## Recompensa en segundos equivalentes de producción pasiva.
const REWARD_SECONDS := 60.0
const PENALTY_SECONDS := 15.0

var _timer: Timer
var _last_triggered_msec: int = -int(COOLDOWN * 1000.0)


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_PAUSABLE
	_timer = Timer.new()
	_timer.one_shot = true
	_timer.timeout.connect(_on_tick)
	add_child(_timer)
	_schedule_next()


func apply_outcome(success: bool) -> void:
	var rate := GameState.state.tokens_per_second * GameState.state.qubit_multiplier
	if rate <= 0.0:
		rate = GameState.state.tokens_per_tap * 5.0
	var bonus := rate * (REWARD_SECONDS if success else -PENALTY_SECONDS)
	if bonus > 0.0:
		GameState.add_tokens(DebugFlags.apply_to_token_yield(bonus))
	elif bonus < 0.0:
		GameState.try_spend_tokens(absf(bonus))
	minigame_outcome_applied.emit(success, bonus)


func _on_tick() -> void:
	if _should_trigger():
		_trigger_random()
	_schedule_next()


func _schedule_next() -> void:
	_timer.start(CHECK_INTERVAL + randf_range(-CHECK_JITTER, CHECK_JITTER))


func _should_trigger() -> bool:
	if SaveService.get_active_slot() == 0:
		return false
	if SceneManager.has_overlay():
		return false
	if GameState.passive_paused:
		return false
	if Time.get_ticks_msec() - _last_triggered_msec < int(COOLDOWN * 1000.0):
		return false
	return randf() < TRIGGER_CHANCE


func _trigger_random() -> void:
	if MINIGAME_SCENES.is_empty():
		return
	_last_triggered_msec = Time.get_ticks_msec()
	var scene_path: String = MINIGAME_SCENES[randi() % MINIGAME_SCENES.size()]
	minigame_offered.emit(scene_path)
