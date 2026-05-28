extends Control

## Pantalla de ajustes accesible desde el menú y desde el HUD.
##
## Volúmenes de música y SFX vía sliders (rango fijo -40..0 dB),
## toggle del multiplicador de evaluación (#298), botón "Volver al
## menú principal" que guarda la partida activa y cambia de escena, y
## botón "Borrar partida" con confirmación que elimina el slot activo
## y vuelve al menú principal.

const MAIN_MENU_SCENE := "res://scenes/screens/main_menu/main_menu.tscn"

const MIN_DB := -40.0
const MAX_DB := 0.0

@onready var _back_button: Button = %BackButton
@onready var _music_slider: HSlider = %MusicSlider
@onready var _sfx_slider: HSlider = %SfxSlider
@onready var _debug_toggle: TextureButton = %DebugToggle
@onready var _back_to_menu_button: Button = %BackToMenuButton
@onready var _delete_button: Button = %DeleteButton
@onready var _confirm_dialog: ConfirmationDialog = %ConfirmDialog


func _ready() -> void:
	_music_slider.min_value = MIN_DB
	_music_slider.max_value = MAX_DB
	_music_slider.value = AudioManager.music_db
	_music_slider.value_changed.connect(AudioManager.set_music_db)
	_sfx_slider.min_value = MIN_DB
	_sfx_slider.max_value = MAX_DB
	_sfx_slider.value = AudioManager.sfx_db
	_sfx_slider.value_changed.connect(AudioManager.set_sfx_db)
	_debug_toggle.button_pressed = DebugFlags.is_eval_multiplier_enabled()
	_debug_toggle.toggled.connect(DebugFlags.set_eval_multiplier_enabled)
	_back_to_menu_button.pressed.connect(_on_back_to_menu_pressed)
	_delete_button.pressed.connect(_on_delete_pressed)
	_back_button.pressed.connect(_close)
	_confirm_dialog.confirmed.connect(_delete_current_slot)
	var no_active_slot := SaveService.get_active_slot() == 0
	_back_to_menu_button.disabled = no_active_slot
	_delete_button.disabled = no_active_slot
	AudioManager.wire_buttons_in(self)


func _on_back_to_menu_pressed() -> void:
	if not persist_active_slot():
		return
	SceneManager.pop_overlay()
	SceneManager.change_scene(MAIN_MENU_SCENE)


## Persiste el slot activo (si lo hay) antes de volver al menú.
##
## Aislada del handler de navegación para poder testearla sin tirar de
## SceneManager.change_scene. Devuelve [code]true[/code] si había slot
## activo y el guardado tuvo éxito.
func persist_active_slot() -> bool:
	var slot := SaveService.get_active_slot()
	if slot == 0:
		return false
	return SaveService.save_now(slot)


func _on_delete_pressed() -> void:
	var slot := SaveService.get_active_slot()
	if slot == 0:
		return
	_confirm_dialog.dialog_text = "Vas a borrar el slot %d activo. Esta acción no se puede deshacer." % slot
	_confirm_dialog.popup_centered()


func _delete_current_slot() -> void:
	var slot := SaveService.get_active_slot()
	if slot == 0:
		return
	SaveService.clear_save(slot)
	SaveService.set_active_slot(0)
	GameState.reset()
	SceneManager.pop_overlay()
	SceneManager.change_scene(MAIN_MENU_SCENE)


func _close() -> void:
	SceneManager.pop_overlay()
