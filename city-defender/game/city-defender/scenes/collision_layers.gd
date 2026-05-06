class_name CollisionLayers

static var CITIES: int = _bit_for("cities")
static var INTERCEPTORS: int = _bit_for("interceptors")
static var ENEMY_MISSILES: int = _bit_for("enemy_missiles")
static var EXPLOSIONS: int = _bit_for("explosions")


static func _bit_for(layer_name: String) -> int:
	for i in range(1, 33):
		var setting := "layer_names/2d_physics/layer_%d" % i
		if not ProjectSettings.has_setting(setting):
			continue
		if ProjectSettings.get_setting(setting) == layer_name:
			return 1 << (i - 1)
	push_error("CollisionLayers: layer '%s' no está definida en project.godot" % layer_name)
	return 0
