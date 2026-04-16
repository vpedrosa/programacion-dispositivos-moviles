extends Node

## Gestión de highscores globales anónimas con Firebase Firestore.
## Usa la API REST de Firestore sobre HTTPS.
## Las credenciales se cargan en runtime desde res://.env (no se commitean).

const _COLLECTION: String = "highscores"
const _TOP_COUNT: int = 10

var _project_id: String = ""
var _api_key: String = ""
var _http: HTTPRequest
var _initialized: bool = false


func _ready() -> void:
	_load_env()
	if _project_id.is_empty() or _api_key.is_empty():
		push_warning("FirebaseManager: credenciales incompletas — las puntuaciones online no funcionarán")
	else:
		_initialized = true
	_http = HTTPRequest.new()
	add_child(_http)


## Devuelve Array[Dictionary] [{score, date}, ...] ordenado por puntuación desc (top 10).
## Usa el endpoint runQuery para orden fiable sin necesitar un índice compuesto.
func get_top_scores() -> Array[Dictionary]:
	if not _initialized:
		push_warning("FirebaseManager: get_top_scores() llamado sin credenciales")
		return []
	var url := (
		"https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents:runQuery?key=%s"
		% [_project_id, _api_key]
	)
	var body := JSON.stringify({
		"structuredQuery": {
			"from": [{"collectionId": _COLLECTION}],
			"orderBy": [{"field": {"fieldPath": "score"}, "direction": "DESCENDING"}],
			"limit": _TOP_COUNT
		}
	})
	_http.request(url, ["Content-Type: application/json"], HTTPClient.METHOD_POST, body)
	var result = await _http.request_completed
	return _parse_query_scores(result[3])


## Envía una puntuación con nombre de jugador. Solo envía si score > 0 y name no está vacío.
func submit_score(score: int, player_name: String) -> void:
	if not _initialized:
		push_warning("FirebaseManager: submit_score() llamado sin credenciales")
		return
	if score <= 0 or player_name.strip_edges().is_empty():
		return
	var url := (
		"https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents/%s?key=%s"
		% [_project_id, _COLLECTION, _api_key]
	)
	var body := JSON.stringify({
		"fields": {
			"score":     {"integerValue": str(score)},
			"name":      {"stringValue": player_name.strip_edges()},
			"timestamp": {"integerValue": str(int(Time.get_unix_time_from_system()))}
		}
	})
	_http.request(url, ["Content-Type: application/json"], HTTPClient.METHOD_POST, body)
	await _http.request_completed


func _parse_query_scores(body: PackedByteArray) -> Array[Dictionary]:
	var text := body.get_string_from_utf8()
	var json = JSON.parse_string(text)
	if not json is Array:
		return []
	var result: Array[Dictionary] = []
	for entry in (json as Array):
		if not entry is Dictionary:
			continue
		var doc = (entry as Dictionary).get("document", null)
		if not doc is Dictionary:
			continue
		var fields: Dictionary = (doc as Dictionary).get("fields", {})
		if not fields.has("score"):
			continue
		var ts: int = int(fields.get("timestamp", {}).get("integerValue", 0))
		var date_str: String = ""
		if ts > 0:
			var dt := Time.get_datetime_dict_from_unix_time(ts)
			date_str = "%02d/%02d/%04d" % [dt.day, dt.month, dt.year]
		result.append({
			"score": int(fields.get("score", {}).get("integerValue", 0)),
			"name":  fields.get("name", {}).get("stringValue", ""),
			"date":  date_str,
		})
	return result


func _load_env() -> void:
	var f := FileAccess.open("res://.env", FileAccess.READ)
	if f == null:
		push_error("FirebaseManager: no se encontró res://.env — las puntuaciones online no funcionarán")
		return
	while not f.eof_reached():
		var line := f.get_line().strip_edges()
		if line.is_empty() or line.begins_with("#"):
			continue
		var idx := line.find("=")
		if idx <= 0:
			continue
		var key := line.left(idx).strip_edges()
		var value := line.right(line.length() - idx - 1).strip_edges()
		match key:
			"FIREBASE_PROJECT_ID": _project_id = value
			"FIREBASE_API_KEY":    _api_key    = value
