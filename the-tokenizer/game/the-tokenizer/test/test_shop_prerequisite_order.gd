extends GutTest

## La tienda debe listar cada prerrequisito por encima de la mejora que lo
## exige, aun cuando el prerrequisito sea más caro que el dependiente (p.ej.
## era_1_serverroom (8000) cuelga de era_1_cluster (20000), así que cluster
## debe aparecer más arriba pese a costar más).

const ShopScene := preload("res://scenes/screens/shop/shop.tscn")
const UPGRADE_CARD_SCRIPT := preload("res://scenes/ui/upgrade_card/upgrade_card.gd")

var _shop: Control


func before_each() -> void:
	SceneManager.clear_overlays()
	GameState.reset(false)


func after_each() -> void:
	if is_instance_valid(_shop):
		_shop.queue_free()
		_shop = null
	await get_tree().process_frame
	GameState.set_era(PlayerState.ERA_BASEMENT)


## Devuelve los id de las mejoras en el orden en que la tienda las pinta.
func _displayed_order(era: int) -> Array:
	GameState.set_era(era)
	_shop = ShopScene.instantiate()
	get_tree().root.add_child(_shop)
	await get_tree().process_frame
	var list: Node = _shop.get_node("%UpgradeList")
	var ids: Array = []
	for card in list.get_children():
		if card.get_script() == UPGRADE_CARD_SCRIPT:
			ids.append(card._upgrade.id)
	return ids


## Cada mejora con prerrequisito debe situarse por debajo de su prerrequisito.
func _assert_prereqs_above(ids: Array, era_label: String) -> void:
	for id in ids:
		var upgrade := UpgradeService.get_by_id(id)
		if upgrade.prerequisite_id == &"":
			continue
		if not ids.has(upgrade.prerequisite_id):
			continue
		assert_lt(ids.find(upgrade.prerequisite_id), ids.find(id),
			"%s: el prerrequisito '%s' debe ir por encima de '%s'" % [
				era_label, upgrade.prerequisite_id, id])


func test_era_1_lists_prerequisites_above_dependents() -> void:
	var ids: Array = await _displayed_order(PlayerState.ERA_BASEMENT)
	_assert_prereqs_above(ids, "Era 1")
	# Caso clave: cluster (más caro) debe ir por encima de serverroom (más
	# barato) porque serverroom lo tiene como prerrequisito.
	assert_lt(ids.find(&"era_1_cluster"), ids.find(&"era_1_serverroom"),
		"era_1_cluster debe aparecer por encima de era_1_serverroom")


func test_era_7_lists_prerequisites_above_dependents() -> void:
	var ids: Array = await _displayed_order(PlayerState.ERA_SINGULARITY)
	_assert_prereqs_above(ids, "Era 7")
	# hyperparams (20000) cuelga de distillation (1000000): pese a ser mucho
	# más barato, debe quedar por debajo.
	assert_lt(ids.find(&"era_7_distillation"), ids.find(&"era_7_hyperparams"),
		"era_7_distillation debe aparecer por encima de era_7_hyperparams")
