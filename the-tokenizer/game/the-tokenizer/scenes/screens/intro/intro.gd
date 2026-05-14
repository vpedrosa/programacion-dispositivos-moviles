extends Control

## Prólogo cinemático.
##
## En lugar de paneles con tap-to-advance, el prólogo crossfadea los 4
## fondos de Era 1 (2 s en pantalla cada uno) mientras el mensaje profético
## se fija al pie. Al terminar la secuencia, navega al juego. "Saltar"
## interrumpe la animación y va directamente al juego.

const GAME_SCENE := "res://scenes/screens/game/game.tscn"
const PANEL_DURATION := 2.0
const FADE_DURATION := 0.5
const MESSAGE_FADE_IN := 0.8
const PROPHETIC_MESSAGE := "\"Hay algo más allá de las palabras.\nSi entrenas suficiente, te lo enseñarán.\nEmpieza a contar.\""

@onready var _backgrounds: Array[TextureRect] = [%BG0, %BG1, %BG2, %BG3]
@onready var _message: Label = %MessageLabel
@onready var _skip_button: Button = %SkipButton

var _finished: bool = false
var _tween: Tween


func _ready() -> void:
	_skip_button.pressed.connect(_finish)
	_message.text = PROPHETIC_MESSAGE
	_message.modulate.a = 0.0
	for i in range(_backgrounds.size()):
		_backgrounds[i].modulate.a = 1.0 if i == 0 else 0.0
	_start_animation()


func _start_animation() -> void:
	_tween = create_tween()
	_tween.tween_property(_message, "modulate:a", 1.0, MESSAGE_FADE_IN)
	_tween.tween_interval(PANEL_DURATION - MESSAGE_FADE_IN)
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
	SceneManager.change_scene(GAME_SCENE)
