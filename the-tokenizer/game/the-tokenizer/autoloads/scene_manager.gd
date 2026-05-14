extends Node

## Gestor central de navegación.
##
## Soporta dos modelos de navegación:
##  * `change_scene(path, fade)` reemplaza la escena raíz (menú → juego → final).
##  * `push_overlay(scene)` / `pop_overlay()` apilan escenas encima de la actual
##    sin destruirla (tienda, eventos éticos, minijuegos, evento cuántico).

signal scene_changed(path: String)
signal overlay_pushed(node: Node)
signal overlay_popped(node: Node)

const FADE_DURATION := 0.25

var _fade_layer: CanvasLayer
var _fade_rect: ColorRect
var _overlay_stack: Array[Node] = []
var _changing := false


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_build_fade_overlay()


func change_scene(path: String, fade: bool = true) -> void:
	if _changing:
		return
	_changing = true
	if fade:
		await _fade_to(1.0)
	var err := get_tree().change_scene_to_file(path)
	if err != OK:
		push_error("SceneManager: no se ha podido cargar '%s' (err %d)" % [path, err])
		_changing = false
		if fade:
			await _fade_to(0.0)
		return
	# Garantiza un frame para que la nueva escena exista antes de revelarla.
	await get_tree().process_frame
	if fade:
		await _fade_to(0.0)
	_changing = false
	scene_changed.emit(path)


func push_overlay(scene: Variant) -> Node:
	var packed: PackedScene
	if scene is PackedScene:
		packed = scene
	elif scene is String:
		packed = load(scene) as PackedScene
	else:
		push_error("SceneManager.push_overlay: tipo no soportado")
		return null
	if packed == null:
		push_error("SceneManager.push_overlay: PackedScene nulo")
		return null
	var instance := packed.instantiate()
	# El overlay vive bajo la raíz para que sobreviva a `change_scene`.
	get_tree().root.add_child(instance)
	_overlay_stack.append(instance)
	overlay_pushed.emit(instance)
	return instance


func pop_overlay() -> void:
	if _overlay_stack.is_empty():
		return
	var top := _overlay_stack.pop_back() as Node
	overlay_popped.emit(top)
	if is_instance_valid(top):
		top.queue_free()


func has_overlay() -> bool:
	return not _overlay_stack.is_empty()


func _build_fade_overlay() -> void:
	_fade_layer = CanvasLayer.new()
	_fade_layer.layer = 128
	_fade_layer.name = "SceneManagerFade"
	add_child(_fade_layer)
	_fade_rect = ColorRect.new()
	_fade_rect.color = Color.BLACK
	_fade_rect.mouse_filter = Control.MOUSE_FILTER_IGNORE
	_fade_rect.set_anchors_preset(Control.PRESET_FULL_RECT)
	_fade_rect.modulate.a = 0.0
	_fade_layer.add_child(_fade_rect)


func _fade_to(alpha: float) -> void:
	var tween := create_tween()
	tween.tween_property(_fade_rect, "modulate:a", alpha, FADE_DURATION)
	await tween.finished
