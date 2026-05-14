extends Node

## Reproducción central de música y SFX.
##
## Mantiene dos AudioStreamPlayer (música + SFX) hijos del autoload, así
## la música no se corta entre cambios de escena. play_ambient(era) elige
## la pista por era y los SFX se disparan vía play_sfx / play_button_sfx.
## wire_buttons_in(node) conecta el SFX de pulsación a todos los Button
## descendientes para ahorrar boilerplate en cada pantalla.

signal music_changed(stream: AudioStream)

const BUTTON_SFX := preload("res://assets/sounds/button-pluck.mp3")
const FADE_OUT_DB := -40.0

const ERA_TRACKS := {
	1: preload("res://assets/sounds/level-1.mp3"),
	7: preload("res://assets/sounds/level-7.mp3"),
}

@export var music_db: float = -6.0
@export var sfx_db: float = -4.0

var _music_player: AudioStreamPlayer
var _sfx_player: AudioStreamPlayer
var _current_music: AudioStream
var _fade_tween: Tween


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_music_player = AudioStreamPlayer.new()
	_music_player.bus = "Master"
	_music_player.volume_db = music_db
	add_child(_music_player)
	_sfx_player = AudioStreamPlayer.new()
	_sfx_player.bus = "Master"
	_sfx_player.volume_db = sfx_db
	add_child(_sfx_player)


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


func wire_buttons_in(root: Node) -> void:
	for button in root.find_children("", "Button", true, false):
		if button is Button and not button.pressed.is_connected(play_button_sfx):
			button.pressed.connect(play_button_sfx)


func set_music_db(db: float) -> void:
	music_db = db
	if not (_fade_tween and _fade_tween.is_running()):
		_music_player.volume_db = db


func set_sfx_db(db: float) -> void:
	sfx_db = db
	_sfx_player.volume_db = db


func _finalise_stop() -> void:
	_music_player.stop()
	_current_music = null
	music_changed.emit(null)
