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
const EXTENSIONS := ["ogg", "wav", "mp3"]

var _sfx_player: AudioStreamPlayer
var _music_player: AudioStreamPlayer
var _sfx_cache: Dictionary = {}


func _ready() -> void:
	_sfx_player = AudioStreamPlayer.new()
	_sfx_player.bus = "SFX"
	add_child(_sfx_player)

	_music_player = AudioStreamPlayer.new()
	_music_player.bus = "Music"
	_music_player.volume_db = -6.0
	add_child(_music_player)


func play_sfx(name: String) -> void:
	var stream := _load_sound(name)
	if stream == null:
		return
	_sfx_player.stream = stream
	_sfx_player.play()


func play_music(name: String) -> void:
	var stream := _load_sound(name)
	if stream == null:
		return
	if _music_player.stream == stream and _music_player.playing:
		return
	_music_player.stream = stream
	_music_player.play()


func stop_music() -> void:
	_music_player.stop()


func _load_sound(name: String) -> AudioStream:
	if _sfx_cache.has(name):
		return _sfx_cache[name]
	for ext in EXTENSIONS:
		var path := SFX_DIR + name + "." + ext
		if ResourceLoader.exists(path):
			var stream: AudioStream = load(path)
			_sfx_cache[name] = stream
			return stream
	_sfx_cache[name] = null  # cache miss so we don't re-check every frame
	return null
