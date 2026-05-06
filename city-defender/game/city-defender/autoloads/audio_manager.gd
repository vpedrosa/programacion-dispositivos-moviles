extends Node

const SFX_DIR := "res://assets/sounds/"
const EXTENSIONS: Array[String] = ["ogg", "wav", "mp3"]

const MUSIC_VOLUME_DB := -6.0
const MUSIC_FADE_IN_DURATION := 3.0
const SFX_POOL_SIZE := 6

var _sfx_pool: Array[AudioStreamPlayer] = []
var _sfx_pool_index: int = 0
var _music_player: AudioStreamPlayer
var _voice_player: AudioStreamPlayer
var _zap_player: AudioStreamPlayer
var _sfx_cache: Dictionary = {}
var _music_tween: Tween = null


func _ready() -> void:
	for i in SFX_POOL_SIZE:
		var p := AudioStreamPlayer.new()
		p.bus = "SFX"
		add_child(p)
		_sfx_pool.append(p)

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
	var player := _next_sfx_player()
	player.stream = stream
	player.play()


func _next_sfx_player() -> AudioStreamPlayer:
	for i in SFX_POOL_SIZE:
		var idx := (_sfx_pool_index + i) % SFX_POOL_SIZE
		if not _sfx_pool[idx].playing:
			_sfx_pool_index = (idx + 1) % SFX_POOL_SIZE
			return _sfx_pool[idx]
	var oldest := _sfx_pool[_sfx_pool_index]
	_sfx_pool_index = (_sfx_pool_index + 1) % SFX_POOL_SIZE
	return oldest


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
	_sfx_cache[sound_name] = null
	return null
