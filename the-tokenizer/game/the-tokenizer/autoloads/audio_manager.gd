extends Node

## Reproducción central de música y SFX.
##
## Mantiene dos AudioStreamPlayer (música + SFX) hijos del autoload, así
## la música no se corta entre cambios de escena. play_ambient(era) elige
## la pista por era y los SFX se disparan vía play_sfx / play_button_sfx.
## wire_buttons_in(node) conecta el SFX de pulsación a todos los Button
## descendientes para ahorrar boilerplate en cada pantalla.

signal music_changed(stream: AudioStream)
signal music_volume_changed(db: float)
signal sfx_volume_changed(db: float)

const BUTTON_SFX := preload("res://assets/sounds/button-pluck.mp3")
const TYPING_LOOP := preload("res://assets/sounds/typing.mp3")
const WIN_JINGLE := preload("res://assets/sounds/win.mp3")
const ACTION_POSITIVE_SFX := preload("res://assets/sounds/action-positive.mp3")
const ACTION_NEUTRAL_SFX := preload("res://assets/sounds/action-neutral.mp3")
const ACTION_NEGATIVE_SFX := preload("res://assets/sounds/action-negative.mp3")
const FADE_OUT_DB := -40.0
const CONFIG_PATH := "user://audio_settings.cfg"

const ERA_TRACKS := {
	1: preload("res://assets/sounds/level-1.mp3"),
	7: preload("res://assets/sounds/level-7.mp3"),
}

@export var music_db: float = -6.0
@export var sfx_db: float = -4.0

var _music_player: AudioStreamPlayer
var _sfx_player: AudioStreamPlayer
var _typing_player: AudioStreamPlayer
var _current_music: AudioStream
var _fade_tween: Tween


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_load_settings()
	_music_player = AudioStreamPlayer.new()
	_music_player.bus = "Master"
	_music_player.volume_db = music_db
	add_child(_music_player)
	_sfx_player = AudioStreamPlayer.new()
	_sfx_player.bus = "Master"
	_sfx_player.volume_db = sfx_db
	add_child(_sfx_player)
	_typing_player = AudioStreamPlayer.new()
	_typing_player.bus = "Master"
	_typing_player.stream = TYPING_LOOP
	_typing_player.volume_db = sfx_db - 4.0
	add_child(_typing_player)


func play_ambient(era: int) -> void:
	var track: AudioStream = ERA_TRACKS.get(era)
	if track == null:
		stop_music(0.4)
		return
	play_music(track, 0.8)


func play_music(stream: AudioStream, fade_in: float = 0.0) -> void:
	if stream == null:
		stop_music(fade_in)
		return
	if _current_music == stream and _music_player.playing:
		return
	_current_music = stream
	if _fade_tween and _fade_tween.is_running():
		_fade_tween.kill()
	_music_player.stream = stream
	if fade_in > 0.0:
		_music_player.volume_db = FADE_OUT_DB
		_music_player.play()
		_fade_tween = create_tween()
		_fade_tween.tween_property(_music_player, "volume_db", music_db, fade_in)
	else:
		_music_player.volume_db = music_db
		_music_player.play()
	music_changed.emit(stream)


func stop_music(fade_out: float = 0.0) -> void:
	if not _music_player.playing:
		return
	if _fade_tween and _fade_tween.is_running():
		_fade_tween.kill()
	if fade_out <= 0.0:
		_music_player.stop()
		_current_music = null
		music_changed.emit(null)
		return
	_fade_tween = create_tween()
	_fade_tween.tween_property(_music_player, "volume_db", FADE_OUT_DB, fade_out)
	_fade_tween.tween_callback(_finalise_stop)


func play_sfx(stream: AudioStream) -> void:
	if stream == null:
		return
	_sfx_player.stream = stream
	_sfx_player.play()


func play_button_sfx() -> void:
	play_sfx(BUTTON_SFX)


## Jingle de victoria al llegar a la pantalla de Ending.
##
## El propio caller (ending.gd) es responsable de no llamarlo más de una
## vez por instancia — aquí no se guarda estado.
func play_win_jingle() -> void:
	play_sfx(WIN_JINGLE)


## SFX al confirmar una decisión ética. El stream elegido depende del
## signo del peso de la opción seleccionada (ver [EthicalEvent]).
func play_ethical_action_sfx(weight: int) -> void:
	if weight > 0:
		play_sfx(ACTION_POSITIVE_SFX)
	elif weight < 0:
		play_sfx(ACTION_NEGATIVE_SFX)
	else:
		play_sfx(ACTION_NEUTRAL_SFX)


func start_typing() -> void:
	if _typing_player.playing:
		return
	_typing_player.play()


func stop_typing() -> void:
	if _typing_player.playing:
		_typing_player.stop()


func wire_buttons_in(root: Node) -> void:
	for button in root.find_children("", "Button", true, false):
		if button is Button and not button.pressed.is_connected(play_button_sfx):
			button.pressed.connect(play_button_sfx)


func set_music_db(db: float) -> void:
	music_db = db
	if not (_fade_tween and _fade_tween.is_running()):
		_music_player.volume_db = db
	_save_settings()
	music_volume_changed.emit(db)


func set_sfx_db(db: float) -> void:
	sfx_db = db
	_sfx_player.volume_db = db
	_typing_player.volume_db = db - 4.0
	_save_settings()
	sfx_volume_changed.emit(db)


func _finalise_stop() -> void:
	_music_player.stop()
	_current_music = null
	music_changed.emit(null)


func _load_settings() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(CONFIG_PATH) != OK:
		return
	music_db = float(cfg.get_value("audio", "music_db", music_db))
	sfx_db = float(cfg.get_value("audio", "sfx_db", sfx_db))


func _save_settings() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("audio", "music_db", music_db)
	cfg.set_value("audio", "sfx_db", sfx_db)
	cfg.save(CONFIG_PATH)
