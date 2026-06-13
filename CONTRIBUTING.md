# Guía de contribución

## Commits

Este proyecto sigue la convención [Conventional Commits](https://www.conventionalcommits.org/es/).

### Formato

```
<tipo> #<issue>: <descripción>
```

Cuando el commit cierra la issue, añadir `Closes #<issue>` en el cuerpo del mensaje.

### Tipos permitidos

| Tipo | Uso |
| --- | --- |
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de errores |
| `docs` | Cambios en documentación |
| `style` | Formato, sin cambios en lógica (espacios, puntos y coma, etc.) |
| `refactor` | Refactorización sin cambio de funcionalidad ni corrección de errores |
| `test` | Añadir o corregir tests |
| `chore` | Tareas de mantenimiento (dependencias, CI, configuración) |

### Reglas

1. Cada commit debe estar asociado a una issue existente.
2. El número de la issue se incluye siempre en el título del commit.
3. Si el commit resuelve completamente la issue, incluir `Closes #<issue>` en el cuerpo.

### Ejemplos

Commit que avanza una issue sin cerrarla:

```
feat #24: añade funcionalidad de guardado de estado
```

Commit que cierra una issue:

```
fix #12: corrige error en la detección de dispositivos

Closes #12
```

Commit de documentación:

```
docs #3: actualiza README con instrucciones de instalación

Closes #3
```

## CI/CD

Cada proyecto Godot del repo dispone de un workflow en `.github/workflows/`
que se dispara con push a `master` y genera artifacts descargables:

- `city-defender-cicd.yml` → APK Android + binario Linux x86_64.
- `the-tokenizer-cicd.yml` → APK Android + binario Linux x86_64.

Ambos workflows comparten los secretos de firma Android del repo:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

El workflow de the-tokenizer ejecuta primero la suite GUT (`addons/gut/gut_cmdln.gd`)
y solo exporta si los tests pasan. El export Android instala primero el
template Gradle con `godot --headless --install-android-build-template`
porque `gradle_build/use_gradle_build=true` en `export_presets.cfg` lo
exige (en local cada dev también debe ejecutar este comando una vez).

## Gestión de assets

Los binarios del juego (sprites `.png`, sonidos `.mp3`, fuentes `.ttf`) se
versionan directamente en el repositorio, pero **siempre en la resolución y
formato mínimos que consume el juego** para no inflar el histórico. Reglas:

1. **Optimiza antes de commitear.** Los assets generados a alta resolución
   (p.ej. iconos a 1024×1024) no se versionan en bruto: se reducen a la
   resolución que usa la UI antes de añadirlos. Para los iconos de tienda hay
   un script de apoyo, `the-tokenizer/game/the-tokenizer/scripts/resize_upgrades.py`.
2. **No versionar copias ni artefactos.** Los `*.bak`/`*.old`/`*.orig` y los
   export `*.apk`/`*.aab` están en `.gitignore`; el histórico de git ya
   conserva las versiones previas, así que no hagas backups manuales dentro
   del repo.
3. **Mantén el original aparte.** Si necesitas conservar la fuente a alta
   resolución de un asset, guárdala fuera del repo (almacenamiento del equipo);
   en el repo va solo la versión optimizada.
4. **Binarios grandes nuevos → Git LFS.** Si en el futuro hay que versionar un
   asset realmente pesado que no se puede optimizar (vídeo, audio largo sin
   comprimir), gestiónalo con Git LFS en lugar de subirlo directo:

   ```
   git lfs install
   git lfs track "*.ogg"   # ejemplo: añade el patrón a .gitattributes
   git add .gitattributes
   ```

   No migramos a LFS los assets ya versionados (requeriría reescribir el
   histórico y romper los clones existentes); LFS aplica solo a binarios
   pesados añadidos a partir de ahora.
