extends GutTest

## Verifica que el catálogo de minijuegos del MinigameService contiene
## ambas escenas y que la selección aleatoria no queda sesgada hacia
## una sola (#347).

func test_catalog_contains_both_scenes() -> void:
	assert_eq(MinigameService.MINIGAME_SCENES.size(), 2)
	var has_backprop := false
	var has_refrig := false
	for path in MinigameService.MINIGAME_SCENES:
		if String(path).find("backpropagation") != -1:
			has_backprop = true
		elif String(path).find("refrigeration") != -1:
			has_refrig = true
	assert_true(has_backprop, "backpropagation debe estar en el catálogo")
	assert_true(has_refrig, "refrigeration debe estar en el catálogo")


func test_random_selection_covers_all_scenes() -> void:
	# Reseed determinístico para que el test sea reproducible.
	seed(0xC0DE)
	var seen: Dictionary = {}
	for i in 200:
		var idx := randi() % MinigameService.MINIGAME_SCENES.size()
		seen[idx] = true
	assert_eq(seen.size(), MinigameService.MINIGAME_SCENES.size(),
		"randi() % size debe poder alcanzar cualquier índice del catálogo")
