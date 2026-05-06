extends GutTest


func test_rank_first_place_when_no_scores_above() -> void:
	var rank := GameOverScreen._compute_personal_rank([100, 80, 50], 100)
	assert_eq(rank, 1)


func test_rank_strict_greater() -> void:
	var rank := GameOverScreen._compute_personal_rank([300, 200, 100], 150)
	assert_eq(rank, 3)


func test_rank_with_ties_uses_competitive_ranking() -> void:
	var rank := GameOverScreen._compute_personal_rank([200, 150, 150, 100], 150)
	assert_eq(rank, 2, "scores iguales comparten ranking; sólo cuenta los estrictamente mayores")


func test_rank_below_all() -> void:
	var rank := GameOverScreen._compute_personal_rank([500, 400, 300], 100)
	assert_eq(rank, 4)


func test_rank_with_empty_array() -> void:
	var rank := GameOverScreen._compute_personal_rank([], 100)
	assert_eq(rank, 1)
