extends Node

## Catálogo y lógica de compra de mejoras.
##
## Carga todos los UpgradeData de [constant UPGRADES_DIR] al arrancar y
## resuelve consultas (por era, por id), comprobaciones de disponibilidad y
## ejecución de compra (debita tokens, sube el nivel y aplica el efecto
## incremental en [GameState]).

signal catalog_loaded()

const UPGRADES_DIR := "res://data/upgrades"

var _catalog: Dictionary = {}


func _ready() -> void:
	_load_catalog()


func get_by_id(id: StringName) -> UpgradeData:
	return _catalog.get(id)


func get_all() -> Array[UpgradeData]:
	var result: Array[UpgradeData] = []
	for upgrade in _catalog.values():
		result.append(upgrade)
	return result


func get_for_era(era: int) -> Array[UpgradeData]:
	var result: Array[UpgradeData] = []
	for upgrade in _catalog.values():
		if upgrade.era == era:
			result.append(upgrade)
	return result


func get_purchasable_for_era(era: int) -> Array[UpgradeData]:
	var result: Array[UpgradeData] = []
	for upgrade in get_for_era(era):
		if not is_max_level(upgrade.id) and _prerequisite_satisfied(upgrade):
			result.append(upgrade)
	return result


func get_current_cost(id: StringName) -> float:
	var upgrade := get_by_id(id)
	if upgrade == null:
		return INF
	return upgrade.cost_at_level(GameState.get_upgrade_level(id))


func is_max_level(id: StringName) -> bool:
	var upgrade := get_by_id(id)
	if upgrade == null:
		return true
	return upgrade.is_max_level(GameState.get_upgrade_level(id))


func is_available(id: StringName) -> bool:
	var upgrade := get_by_id(id)
	if upgrade == null:
		return false
	if is_max_level(id):
		return false
	if not _prerequisite_satisfied(upgrade):
		return false
	if upgrade.era != GameState.state.current_era:
		return false
	return true


func can_afford(id: StringName) -> bool:
	return GameState.state.tokens >= get_current_cost(id)


func try_purchase(id: StringName) -> bool:
	if not is_available(id) or not can_afford(id):
		return false
	var upgrade := get_by_id(id)
	var cost := get_current_cost(id)
	if not GameState.try_spend_tokens(cost):
		return false
	GameState.increment_upgrade_level(id)
	_apply_effect(upgrade)
	return true


## Re-aplica los efectos derivados del catálogo sobre GameState.
##
## Útil cuando se cambian valores del catálogo entre versiones para que la
## producción se recalcule sin tener que regalar tokens al jugador.
func recompute_derived_stats() -> void:
	GameState.set_tokens_per_tap(1.0)
	GameState.set_tokens_per_second(0.0)
	GameState.set_qubit_multiplier(1.0)
	for id_str in GameState.state.upgrade_levels.keys():
		var upgrade := get_by_id(StringName(id_str))
		if upgrade == null:
			continue
		var level := int(GameState.state.upgrade_levels[id_str])
		for _i in range(level):
			_apply_effect(upgrade)


func _load_catalog() -> void:
	_catalog.clear()
	if not DirAccess.dir_exists_absolute(UPGRADES_DIR):
		catalog_loaded.emit()
		return
	var dir := DirAccess.open(UPGRADES_DIR)
	if dir == null:
		catalog_loaded.emit()
		return
	dir.list_dir_begin()
	var file_name := dir.get_next()
	while file_name != "":
		if not dir.current_is_dir():
			# En el APK, los .tres se reempaquetan como .tres.remap (Godot
			# 4 los convierte a binario y deja el remap para redirigir
			# el path original). Sin canonicalizar, el iterador no ve
			# ninguna mejora en móvil y la tienda queda vacía.
			var canonical := file_name
			if canonical.ends_with(".remap"):
				canonical = canonical.substr(0, canonical.length() - ".remap".length())
			if canonical.ends_with(".tres"):
				var resource := load(UPGRADES_DIR + "/" + canonical)
				if resource is UpgradeData and resource.id != &"":
					_catalog[resource.id] = resource
				elif resource != null:
					push_warning("UpgradeService: %s no es un UpgradeData válido" % canonical)
		file_name = dir.get_next()
	catalog_loaded.emit()


func _prerequisite_satisfied(upgrade: UpgradeData) -> bool:
	if upgrade.prerequisite_id == &"":
		return true
	return GameState.get_upgrade_level(upgrade.prerequisite_id) > 0


func _apply_effect(upgrade: UpgradeData) -> void:
	match upgrade.effect_type:
		UpgradeData.EffectType.NONE:
			pass
		UpgradeData.EffectType.TOKENS_PER_TAP_ADD:
			GameState.set_tokens_per_tap(GameState.state.tokens_per_tap + upgrade.effect_value)
		UpgradeData.EffectType.TOKENS_PER_TAP_MULT:
			GameState.set_tokens_per_tap(GameState.state.tokens_per_tap * upgrade.effect_value)
		UpgradeData.EffectType.TOKENS_PER_SECOND_ADD:
			GameState.set_tokens_per_second(GameState.state.tokens_per_second + upgrade.effect_value)
		UpgradeData.EffectType.TOKENS_PER_SECOND_MULT:
			GameState.set_tokens_per_second(GameState.state.tokens_per_second * upgrade.effect_value)
		UpgradeData.EffectType.QUBIT_MULTIPLIER_ADD:
			GameState.set_qubit_multiplier(GameState.state.qubit_multiplier + upgrade.effect_value)
