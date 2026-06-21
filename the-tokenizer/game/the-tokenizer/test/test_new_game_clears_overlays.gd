extends GutTest

## Regresión del bug "tras llegar a Era 7, salir al menú y empezar partida
## nueva, el tap manual de Era 1 no responde".
##
## Causa: tap_target._on_tap() se traga el input si SceneManager.has_overlay().
## El stack de overlays no forma parte de GameState.state, así que reset()/
## load_from() no lo limpian. Los flujos de partida nueva, cargar slot y volver
## al menú hacían un solo pop_overlay() que sólo quitaba su propio overlay y
## dejaban colgado cualquier residuo de la sesión anterior. Deben usar
## clear_overlays() (mismo patrón que el reset cuántico, #372).

const SlotPickerScene := preload("res://scenes/screens/slot_picker/slot_picker.tscn")
const SettingsScene := preload("res://scenes/screens/settings/settings.tscn")
const MAX_SLOTS := 3


func before_each() -> void:
	SceneManager.clear_overlays()
	GameState.reset(false)
	_clear_all_slots()
	# Hace que SceneManager.change_scene haga early-return: así los handlers
	# ejercitan su limpieza de overlays sin cambiar la escena raíz real del
	# runner de GUT (la parte que nos interesa ocurre antes del change_scene).
	SceneManager._changing = true


func after_each() -> void:
	SceneManager._changing = false
	SceneManager.clear_overlays()
	SaveService.set_active_slot(0)
	_clear_all_slots()


## Un overlay residual en el stack debe desaparecer al arrancar partida nueva,
## de modo que has_overlay() vuelva a ser false y el tap de Era 1 responda.
func test_start_new_game_clears_stale_overlay() -> void:
	var picker: Control = SlotPickerScene.instantiate()
	add_child_autofree(picker)
	await get_tree().process_frame
	# Simula un residuo de la sesión anterior (p.ej. un overlay de Era 7 que
	# nunca se popeó) seguido del propio overlay del slot_picker.
	_push_dummy_overlay()
	_push_dummy_overlay()
	assert_true(SceneManager.has_overlay(),
		"Precondición: hay overlays en el stack antes de empezar partida")

	picker._start_new_in(1)
	assert_false(SceneManager.has_overlay(),
		"Empezar partida nueva debe vaciar el stack, no sólo popear uno")


## Cargar un slot ocupado también debe dejar el stack limpio.
func test_load_slot_clears_stale_overlay() -> void:
	# Deja una partida guardada en el slot 1 para poder cargarla.
	SaveService.set_active_slot(1)
	SaveService.save_now(1)
	SaveService.set_active_slot(0)

	var picker: Control = SlotPickerScene.instantiate()
	add_child_autofree(picker)
	await get_tree().process_frame
	_push_dummy_overlay()
	_push_dummy_overlay()
	assert_true(SceneManager.has_overlay(), "Precondición: hay overlays")

	picker._load_slot(1)
	assert_false(SceneManager.has_overlay(),
		"Cargar slot debe vaciar el stack para no bloquear el tap")


## Borrar la partida activa desde ajustes y volver al menú debe limpiar el stack.
func test_settings_delete_clears_stale_overlay() -> void:
	SaveService.set_active_slot(1)
	SaveService.save_now(1)

	var settings: Control = SettingsScene.instantiate()
	add_child_autofree(settings)
	await get_tree().process_frame
	_push_dummy_overlay()
	_push_dummy_overlay()
	assert_true(SceneManager.has_overlay(), "Precondición: hay overlays")

	settings._delete_current_slot()
	assert_false(SceneManager.has_overlay(),
		"Borrar partida debe vaciar el stack al volver al menú")


func _push_dummy_overlay() -> void:
	# clear_overlays() hace queue_free de cada overlay; un Control vacío basta
	# para ocupar el stack sin tirar de escenas reales.
	SceneManager.push_overlay(_dummy_packed_scene())


func _dummy_packed_scene() -> PackedScene:
	var node := Control.new()
	var packed := PackedScene.new()
	packed.pack(node)
	node.free()
	return packed


func _clear_all_slots() -> void:
	for slot in range(1, MAX_SLOTS + 1):
		var path := "user://savegame_%d.json" % slot
		if FileAccess.file_exists(path):
			DirAccess.remove_absolute(path)
