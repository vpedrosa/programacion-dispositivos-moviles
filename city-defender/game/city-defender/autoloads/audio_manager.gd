## AudioManager — gestión centralizada de SFX y música.
## Pon los ficheros en res://assets/sounds/ con estos nombres:
##   explosion.ogg  → explosión en el aire
##   impact.ogg     → misil impacta ciudad
##   launch.ogg     → disparo del defensor
##   emp.ogg        → bomba EMP
##   game_over.ogg  → fin de partida
##   music_game.ogg → música en bucle durante la partida
##   music_menu.ogg → música del menú principal
extends Node

const SFX_DIR := "res://assets/sounds/"
const EXTENSIONS: Array[String] = ["ogg", "wav", "mp3"]

const MUSIC_VOLUME_DB := -6.0
const MUSIC_FADE_IN_DURATION := 3.0

var _sfx_player: AudioStreamPlayer
var _music_player: AudioStreamPlayer
var _voice_player: AudioStreamPlayer
var _zap_player: AudioStreamPlayer
var _sfx_cache: Dictionary = {}
var _music_tween: Tween = null


func _ready() -> void:
	_sfx_player = AudioStreamPlayer.new()
	_sfx_player.bus = "SFX"
	add_child(_sfx_player)

	_music_player = AudioStreamPlayer.new()
	_music_player.bus = "Music"
	_music_player.volume_db = MUSIC_VOLUME_DB
	add_child(_music_player)

	_voice_player = AudioStreamPlayer.new()
	_voice_player.bus = "SFX"
	_voice_player.process_mode = Node.PROCESS_MODE_ALWAYS
	add_child(_voice_player)

	_zap_player = AudioStreamPlayer.new()
	_zap_player.bus = "SFX"
	add_child(_zap_player)


func play_sfx(sound_name: String) -> void:
	var stream := _load_sound(sound_name)
	if stream == null:
		return
	_sfx_player.stream = stream
	_sfx_player.play()


func play_zap(sound_name: String) -> void:
	var stream := _load_sound(sound_name)
	if stream == null:
		return
	_zap_player.stream = stream
	_zap_player.play()


func play_voice(sound_name: String) -> void:
	var stream := _load_sound(sound_name)
	if stream == null:
		return
	_voice_player.stream = stream
	_voice_player.play()


func play_music(sound_name: String) -> void:
	var stream := _load_sound(sound_name)
	if stream == null:
		return
	if stream is AudioStreamMP3:
		(stream as AudioStreamMP3).loop = true
	elif stream is AudioStreamOggVorbis:
		(stream as AudioStreamOggVorbis).loop = true
	elif stream is AudioStreamWAV:
		(stream as AudioStreamWAV).loop_mode = AudioStreamWAV.LOOP_FORWARD
	if _music_player.stream == stream and _music_player.playing:
		return
	_music_player.stream = stream
	_music_player.volume_db = -80.0
	_music_player.play()
	if _music_tween:
		_music_tween.kill()
	_music_tween = create_tween()
	_music_tween.tween_property(_music_player, "volume_db", MUSIC_VOLUME_DB, MUSIC_FADE_IN_DURATION)


func stop_music() -> void:
	if _music_tween:
		_music_tween.kill()
		_music_tween = null
	_music_player.stop()
	_music_player.volume_db = MUSIC_VOLUME_DB


func _load_sound(sound_name: String) -> AudioStream:
	if _sfx_cache.has(sound_name):
		return _sfx_cache[sound_name]
	for ext in EXTENSIONS:
		var path := SFX_DIR + sound_name + "." + ext
		if ResourceLoader.exists(path):
			var stream: AudioStream = load(path)
			_sfx_cache[sound_name] = stream
			return stream
	_sfx_cache[sound_name] = null  # cache miss so we don't re-check every frame
	return null
