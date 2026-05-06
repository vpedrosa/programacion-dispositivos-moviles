class_name PowerupData
extends Resource

@export var id: int = 0
@export var name_key: String = ""
@export var desc_key: String = ""
@export var cost: int = 0
@export var icon: Texture2D = null
@export var max_purchases: int = -1


static func make(p_id: int, p_name_key: String, p_desc_key: String, p_cost: int, p_icon_path: String, p_max_purchases: int = -1) -> PowerupData:
	var d := PowerupData.new()
	d.id = p_id
	d.name_key = p_name_key
	d.desc_key = p_desc_key
	d.cost = p_cost
	d.max_purchases = p_max_purchases
	if not p_icon_path.is_empty() and ResourceLoader.exists(p_icon_path):
		d.icon = load(p_icon_path)
	return d
