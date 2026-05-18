extends Node

## Flags para acelerar la evaluación académica.
##
## El multiplicador se aplica únicamente sobre la generación de tokens
## (tap y pasiva): los qubits y el progreso del jefe permanecen intactos
## para no falsear la mecánica cuántica ni la curva de eras.

signal eval_multiplier_changed(enabled: bool)

const CONFIG_PATH := "user://debug_flags.cfg"
const EVAL_MULTIPLIER := 1000.0

var _eval_multiplier_enabled: bool = false


func _ready() -> void:
	_load()


func is_eval_multiplier_enabled() -> bool:
	return _eval_multiplier_enabled


func set_eval_multiplier_enabled(value: bool) -> void:
	if _eval_multiplier_enabled == value:
		return
	_eval_multiplier_enabled = value
	_save()
	eval_multiplier_changed.emit(value)


## Tokens "extra" que el multiplicador añade encima de la cantidad base.
##
## Estos tokens van al balance gastable únicamente (vía
## [method GameState.add_debug_bonus]) para no falsear lifetime_tokens
## ni era_lifetime_tokens — y por tanto no acelerar boss, eventos éticos
## ni la entrada a Era 7.
func bonus_for(amount: float) -> float:
	if not _eval_multiplier_enabled or amount <= 0.0:
		return 0.0
	return amount * (EVAL_MULTIPLIER - 1.0)


func _load() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(CONFIG_PATH) == OK:
		_eval_multiplier_enabled = bool(cfg.get_value("debug", "eval_multiplier", false))


func _save() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("debug", "eval_multiplier", _eval_multiplier_enabled)
	cfg.save(CONFIG_PATH)
