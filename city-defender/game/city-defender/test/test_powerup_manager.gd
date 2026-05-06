extends GutTest

const CITY_SCENE := preload("res://scenes/entities/city/city.tscn")
const DEFENSE_BASE_SCENE := preload("res://scenes/entities/defense_base/defense_base.tscn")

var pm: PowerupManager
var base: DefenseBase


func before_each() -> void:
	pm = PowerupManager.new()
	add_child_autofree(pm)
	base = DEFENSE_BASE_SCENE.instantiate()
	add_child_autofree(base)
	pm.defense_base = base


func _make_city() -> City:
	var c: City = CITY_SCENE.instantiate()
	add_child_autofree(c)
	return c


func test_repair_city_heals_first_damaged() -> void:
	var c := _make_city()
	c.health = 50
	pm.apply_powerup(PowerupId.REPAIR_CITY, [c])
	assert_eq(c.health, City.MAX_HEALTH)


func test_repair_city_skips_destroyed_cities() -> void:
	var dead := _make_city()
	dead.is_alive = false
	dead.health = 0
	var damaged := _make_city()
	damaged.health = 50
	pm.apply_powerup(PowerupId.REPAIR_CITY, [dead, damaged])
	assert_eq(damaged.health, City.MAX_HEALTH)
	assert_eq(dead.health, 0)


func test_repair_city_no_op_when_all_full() -> void:
	var c1 := _make_city()
	var c2 := _make_city()
	pm.apply_powerup(PowerupId.REPAIR_CITY, [c1, c2])
	assert_eq(c1.health, City.MAX_HEALTH)
	assert_eq(c2.health, City.MAX_HEALTH)


func test_rebuild_city_revives_first_destroyed() -> void:
	var c := _make_city()
	c.is_alive = false
	c.health = 0
	pm.apply_powerup(PowerupId.REBUILD_CITY, [c])
	assert_true(c.is_alive)
	assert_gt(c.health, 0)


func test_activate_shield_sets_flag_on_alive_cities() -> void:
	var c1 := _make_city()
	var c2 := _make_city()
	c2.is_alive = false
	pm.apply_powerup(PowerupId.SHIELD, [c1, c2])
	assert_true(c1.has_shield)
	assert_false(c2.has_shield, "ciudades destruidas no reciben escudo")


func test_radius_plus_increments_explosion_bonus() -> void:
	pm.apply_powerup(PowerupId.RADIUS_PLUS, [])
	assert_almost_eq(base._explosion_radius_bonus, PowerupManager.RADIUS_BONUS, 0.001)


func test_cooldown_plus_reduces_cooldown() -> void:
	var initial := base.base_cooldown
	pm.apply_powerup(PowerupId.COOLDOWN_PLUS, [])
	assert_lt(base.base_cooldown, initial)


func test_cooldown_plus_floors_at_min() -> void:
	for i in 100:
		pm.apply_powerup(PowerupId.COOLDOWN_PLUS, [])
	assert_almost_eq(base.base_cooldown, DefenseBase.MIN_COOLDOWN, 0.001)


func test_turret_speed_upgrade() -> void:
	var initial := base._rotation_speed
	pm.apply_powerup(PowerupId.TURRET_SPEED, [])
	assert_almost_eq(base._rotation_speed - initial, PowerupManager.ROTATION_SPEED_BONUS, 0.001)


func test_enable_gatling_sets_flag() -> void:
	pm.apply_powerup(PowerupId.GATLING, [])
	assert_true(base._gatling_active)
