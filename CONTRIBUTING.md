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
