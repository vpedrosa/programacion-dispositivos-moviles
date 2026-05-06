extends GutTest


func test_make_sets_basic_fields() -> void:
	var d := PowerupData.make(PowerupId.REPAIR_CITY, "PU_REPAIR_NAME", "PU_REPAIR_DESC", 200, "")
	assert_eq(d.id, PowerupId.REPAIR_CITY)
	assert_eq(d.name_key, "PU_REPAIR_NAME")
	assert_eq(d.desc_key, "PU_REPAIR_DESC")
	assert_eq(d.cost, 200)
	assert_eq(d.max_purchases, -1, "por defecto, sin límite de compras")


func test_make_sets_max_purchases() -> void:
	var d := PowerupData.make(PowerupId.GATLING, "PU_GATLING_NAME", "PU_GATLING_DESC", 150, "", 1)
	assert_eq(d.max_purchases, 1)


func test_make_skips_icon_when_path_empty() -> void:
	var d := PowerupData.make(PowerupId.EMP, "PU_EMP_NAME", "PU_EMP_DESC", 300, "")
	assert_null(d.icon)


func test_make_skips_icon_when_path_does_not_exist() -> void:
	var d := PowerupData.make(PowerupId.EMP, "PU_EMP_NAME", "PU_EMP_DESC", 300, "res://no/existe.png")
	assert_null(d.icon)
