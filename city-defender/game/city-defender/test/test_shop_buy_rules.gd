extends GutTest


func _make_powerup(cost: int, max_purchases: int = -1) -> PowerupData:
	return PowerupData.make(PowerupId.REPAIR_CITY, "PU_REPAIR_NAME", "PU_REPAIR_DESC", cost, "", max_purchases)


func test_disabled_when_money_insufficient() -> void:
	var data := _make_powerup(200)
	assert_true(Shop.is_buy_disabled(data, 0, 199))


func test_enabled_when_money_exactly_matches() -> void:
	var data := _make_powerup(200)
	assert_false(Shop.is_buy_disabled(data, 0, 200))


func test_enabled_when_money_above_cost() -> void:
	var data := _make_powerup(50)
	assert_false(Shop.is_buy_disabled(data, 0, 9999))


func test_disabled_when_max_purchases_reached() -> void:
	var data := _make_powerup(50, 1)
	assert_true(Shop.is_buy_disabled(data, 1, 9999))


func test_enabled_below_max_purchases() -> void:
	var data := _make_powerup(50, 3)
	assert_false(Shop.is_buy_disabled(data, 2, 9999))


func test_disabled_at_exactly_max_purchases() -> void:
	var data := _make_powerup(50, 3)
	assert_true(Shop.is_buy_disabled(data, 3, 9999))


func test_max_purchases_disables_even_with_enough_money() -> void:
	var data := _make_powerup(10, 1)
	assert_true(Shop.is_buy_disabled(data, 1, 1_000_000))


func test_no_max_purchases_only_money_matters() -> void:
	var data := _make_powerup(100, -1)
	assert_false(Shop.is_buy_disabled(data, 999, 100))
	assert_true(Shop.is_buy_disabled(data, 999, 99))
