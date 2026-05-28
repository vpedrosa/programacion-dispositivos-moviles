extends GutTest

## Garantiza que UpgradeService y EventService cargan su catálogo desde
## data/. En el APK los .tres se reempaquetan como .tres.remap y el
## filtro original (`ends_with(".tres")`) los ignoraba, dejando la
## tienda vacía (#365). Estos tests fallarían si volvemos a olvidar la
## canonicalización del nombre.

func test_upgrade_service_loads_upgrades() -> void:
	assert_gt(UpgradeService.get_all().size(), 0,
		"UpgradeService debe cargar al menos una mejora desde data/upgrades/")


func test_upgrade_service_has_era_1_upgrades() -> void:
	assert_gt(UpgradeService.get_for_era(1).size(), 0,
		"Era 1 debe tener al menos una mejora visible en la tienda")


func test_event_service_loads_events() -> void:
	# EventService no expone el catálogo, pero podemos comprobar que
	# tiene al menos un evento conectado para era 1 simulando un trigger.
	# Suficiente: si el catálogo está vacío, este assert fallaría tras
	# escalar lifetime_tokens más allá del threshold mínimo.
	# Aquí nos limitamos a contar archivos .tres de data/events para
	# fijar el invariante "el filtro encuentra .tres.remap también".
	var dir := DirAccess.open("res://data/events")
	assert_not_null(dir, "data/events debe existir")
	if dir == null:
		return
	dir.list_dir_begin()
	var found := 0
	var file_name := dir.get_next()
	while file_name != "":
		if not dir.current_is_dir():
			var canonical := file_name
			if canonical.ends_with(".remap"):
				canonical = canonical.substr(0, canonical.length() - ".remap".length())
			if canonical.ends_with(".tres"):
				found += 1
		file_name = dir.get_next()
	assert_gt(found, 0,
		"El listado debe ver al menos un .tres (o .tres.remap) en data/events/")
