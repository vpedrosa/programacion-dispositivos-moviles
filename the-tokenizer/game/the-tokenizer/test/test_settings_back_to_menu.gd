extends GutTest

## Cubre persist_active_slot de settings.gd (#370): si hay slot activo
## debe llamar a SaveService.save_now antes de que el handler cambie
## de escena.

const SettingsScene := preload("res://scenes/screens/settings/settings.tscn")
const MAX_SLOTS := 3

var _settings: Control


func before_each() -> void:
	_clear_all_slots()


func after_each() -> void:
	if is_instance_valid(_settings):
		_settings.queue_free()
		_settings = null
	SaveService.set_active_slot(0)
	_clear_all_slots()


func test_persist_returns_false_when_no_active_slot() -> void:
	SaveService.set_active_slot(0)
	_settings = SettingsScene.instantiate()
	add_child_autofree(_settings)
	await get_tree().process_frame
	assert_false(_settings.persist_active_slot(),
		"sin slot activo, persist_active_slot debe devolver false")


func test_persist_calls_save_now_for_active_slot() -> void:
	SaveService.set_active_slot(1)
	watch_signals(SaveService)
	_settings = SettingsScene.instantiate()
	add_child_autofree(_settings)
	await get_tree().process_frame

	assert_true(_settings.persist_active_slot(),
		"con slot activo, persist_active_slot debe devolver true")
	assert_signal_emitted(SaveService, "saved",
		"persist_active_slot debe disparar SaveService.saved")
	assert_true(SaveService.has_save(1),
		"tras persist_active_slot debe existir el fichero del slot 1")


func _clear_all_slots() -> void:
	for slot in range(1, MAX_SLOTS + 1):
		var path := "user://savegame_%d.json" % slot
		if FileAccess.file_exists(path):
			DirAccess.remove_absolute(path)
