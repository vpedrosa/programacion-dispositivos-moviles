extends Control

## Cinemática de entrada a Era 7.
##
## Se navega aquí desde boss.gd justo después de confirmar el hito de
## Era 1. Crossfadea level7/bg0 → bg1 → bg2 → bg3 (2 s cada uno) y al
## terminar (o al pulsar Saltar) hace GameState.set_era(SINGULARITY) y
## navega al juego, de modo que el HUD ya carga con la era 7 aplicada y
## el diálogo de entrada de era se dispara desde game.gd.
##
## Cargar el slot directamente desde Continuar no pasa por este scene,
## así que la cinemática sólo se ve la primera vez que se desbloquea
## Era 7 (y se vuelve a ver tras un reset cuántico, intencionalmente).

const GAME_SCENE := "res://scenes/screens/game/game.tscn"
const PANEL_DURATION := 2.0
const FADE_DURATION := 0.5

@onready var _backgrounds: Array[TextureRect] = [%BG0, %BG1, %BG2, %BG3]
@onready var _skip_button: Button = %SkipButton

var _tween: Tween
var _finished: bool = false


func _ready() -> void:
	GameState.passive_paused = true
	_skip_button.pressed.connect(_finish)
	for i in range(_backgrounds.size()):
		_backgrounds[i].modulate.a = 1.0 if i == 0 else 0.0
	AudioManager.play_ambient(PlayerState.ERA_SINGULARITY)
	_start_crossfade()


func _exit_tree() -> void:
	GameState.passive_paused = false


func _start_crossfade() -> void:
	_tween = create_tween()
	_tween.tween_interval(PANEL_DURATION)
	for i in range(1, _backgrounds.size()):
		_tween.tween_property(_backgrounds[i - 1], "modulate:a", 0.0, FADE_DURATION)
		_tween.parallel().tween_property(_backgrounds[i], "modulate:a", 1.0, FADE_DURATION)
		_tween.tween_interval(PANEL_DURATION - FADE_DURATION)
	_tween.tween_callback(_finish)


func _finish() -> void:
	if _finished:
		return
	_finished = true
	if _tween and _tween.is_running():
		_tween.kill()
	GameState.set_era(PlayerState.ERA_SINGULARITY)
	SceneManager.change_scene(GAME_SCENE)
