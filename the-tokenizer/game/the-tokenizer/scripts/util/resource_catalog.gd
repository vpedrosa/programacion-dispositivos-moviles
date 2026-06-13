class_name ResourceCatalog
extends RefCounted

## Helper de carga de catálogos de recursos desde un directorio de .tres.
##
## Comparte la lógica que antes duplicaban UpgradeService y EventService:
## recorre el directorio y carga cada recurso .tres, canonicalizando el
## sufijo .remap del APK. En el APK los .tres se reempaquetan como
## .tres.remap (Godot 4 los convierte a binario y deja el remap para
## redirigir el path original); sin canonicalizar, el iterador no ve
## ningún recurso en móvil y el catálogo queda vacío.
##
## Devuelve los recursos cargados (no nulos) sin filtrar por tipo: cada
## servicio valida el tipo concreto que espera y emite sus propios avisos.


static func load_dir(dir_path: String) -> Array[Resource]:
	var result: Array[Resource] = []
	if not DirAccess.dir_exists_absolute(dir_path):
		return result
	var dir := DirAccess.open(dir_path)
	if dir == null:
		return result
	dir.list_dir_begin()
	var file_name := dir.get_next()
	while file_name != "":
		if not dir.current_is_dir():
			var canonical := file_name
			if canonical.ends_with(".remap"):
				canonical = canonical.substr(0, canonical.length() - ".remap".length())
			if canonical.ends_with(".tres"):
				var resource := load(dir_path + "/" + canonical)
				if resource != null:
					result.append(resource)
		file_name = dir.get_next()
	return result
