extends Node

## Firebase Firestore manager for global highscores.
## Uses the Firestore REST API over HTTPS.
## Credentials are loaded at runtime from res://.env (never committed to git).

const _COLLECTION: String = "highscores"
const _TOP_COUNT: int = 10

var _project_id: String = ""
var _api_key: String = ""
var _http: HTTPRequest


func _ready() -> void:
	_load_env()
	_http = HTTPRequest.new()
	add_child(_http)


## Returns an Array of Dictionaries [{name, score}, ...] sorted by score desc.
func get_top_scores() -> Array:
	var url := _build_query_url()
	_http.request(url)
	var result = await _http.request_completed
	return _parse_scores(result[3])


## Returns true if score would enter the current top 10.
func is_top_10(score: int) -> bool:
	var scores := await get_top_scores()
	if scores.size() < _TOP_COUNT:
		return true
	return score > scores[-1].score


## Inserts a new entry and removes the lowest if the list exceeds 10.
func submit_score(player_name: String, score: int) -> void:
	var scores := await get_top_scores()

	# Only submit if it qualifies
	if scores.size() >= _TOP_COUNT and score <= scores[-1].score:
		return

	# Write new document
	var url := _build_document_url()
	var body := JSON.stringify({
		"fields": {
			"name":      {"stringValue": player_name},
			"score":     {"integerValue": str(score)},
			"timestamp": {"integerValue": str(int(Time.get_unix_time_from_system()))}
		}
	})
	_http.request(url, ["Content-Type: application/json"], HTTPClient.METHOD_POST, body)
	await _http.request_completed

	# Delete the 11th entry if needed
	if scores.size() >= _TOP_COUNT:
		_delete_document(scores[-1].get("_doc_name", ""))


func _delete_document(doc_name: String) -> void:
	if doc_name.is_empty():
		return
	var url := (
		"https://firestore.googleapis.com/v1/%s?key=%s" % [doc_name, _api_key]
	)
	_http.request(url, [], HTTPClient.METHOD_DELETE, "")
	await _http.request_completed


func _build_query_url() -> String:
	return (
		"https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents/%s"
		% [_project_id, _COLLECTION]
		+ "?orderBy=score%%20desc&pageSize=%d&key=%s" % [_TOP_COUNT, _api_key]
	)


func _build_document_url() -> String:
	return (
		"https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents/%s?key=%s"
		% [_project_id, _COLLECTION, _api_key]
	)


func _parse_scores(body: PackedByteArray) -> Array:
	var text := body.get_string_from_utf8()
	var json = JSON.parse_string(text)
	if json == null or not json.has("documents"):
		return []
	var result: Array = []
	for doc in json["documents"]:
		var fields = doc.get("fields", {})
		result.append({
			"name":      fields.get("name",  {}).get("stringValue",  ""),
			"score":     int(fields.get("score", {}).get("integerValue", 0)),
			"_doc_name": doc.get("name", ""),  # full resource path, needed for delete
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
