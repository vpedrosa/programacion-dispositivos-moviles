# Prompts de imágenes — The Tokenizer

Inventario de todos los assets gráficos del proyecto con la descripción del
**contenido** de cada imagen.

ESTILO: pixel art 2D estilo RPG cozy de granja y fantasía, con vista cenital en perspectiva 3/4 ortográfica, estética cálida, alegre y artesanal; usa una paleta saturada pero armoniosa con verdes vivos, tierras doradas, maderas rojizas, piedra gris azulada y tejados de terracota, contornos limpios en marrón oscuro o azul oscuro, sombreado por bloques de píxeles con alto contraste suave, pequeñas luces en bordes superiores y sombras hacia abajo/derecha; diseña assets modulares tipo tilemap, legibles a tamaño pequeño, con siluetas claras. Evita realismo, blur, degradados suaves, render 3D o exceso de anti-aliasing, priorizando píxeles nítidos colocados a mano, texturas repetibles, ambiente acogedor, colorido y ligeramente fantástico.

## Convenciones de este documento

- Cada imagen tiene su ruta relativa a `game/the-tokenizer/assets/sprites/`.
- Si la imagen forma parte de una **secuencia narrativa** (intros, animación de
  habla, crossfades), se indica el orden y qué transición la enlaza con la
  siguiente.
- Las imágenes destinadas a UI (marcos, botones, slots) deben ser
  **idiomáticas** — tener un rectángulo interior "vacío" donde encajar texto o
  contenido — y describen explícitamente esa zona.
- Era 1 transcurre **en 2010, en el sótano de un piso abandonado** (ver
  `scenes/screens/intro/intro.gd`). Todo el atrezzo de Era 1 es **físico,
  antiguo y reconocible**: hardware con casi una década de uso, papel, cables,
  iluminación pobre. Nada futurista.
- Era 7 transcurre **tras la singularidad**. Estética opuesta a Era 1:
  abstracta, intangible, sin materia identificable.

---

## 1. Personaje — Investigador

Aparece como busto recortado en los paneles de diálogo (`dialogue.tscn`). Se
ven cinco frames: un idle "silencioso", un idle "hablando" (boca cerrada en
posición de habla) y tres frames cíclicos de boca abierta para la animación.

### `player/researcher-idle.png`

Busto del investigador, expresión neutra, boca cerrada, mirando ligeramente a
cámara. Hombros y parte superior del torso visibles. Iluminación frontal
tenue.

### `player/researcher-talking-idle.png`

Mismo encuadre y pose que el anterior, pero con la mandíbula ligeramente
relajada — primera posición del ciclo de habla, antes de abrir la boca.

### `player/researcher-talking-animation-1.png`

Boca semi-abierta, labios formando una vocal abierta (estilo "ah" / "oh").
Resto de la cara idéntico al idle.

### `player/researcher-talking-animation-2.png`

Boca más abierta, articulando una consonante explosiva o vocal larga. Misma
cabeza, sin cambio de inclinación.

### `player/researcher-talking-animation-3.png`

Posición intermedia entre la 1 y la 2, mandíbula apenas abierta, labios
cerrándose. Es el frame que cierra el bucle de tres.

> Importante: los 5 frames tienen que poder superponerse con `modulate.a` sin
> que el cráneo o los hombros se desplacen ni un píxel — solo cambia la zona
> de la boca.

---

## 2. Fondos cinemáticos

Cada era tiene 4 fondos que se cruzan en secuencia para construir la entrada
narrativa. En `scenes/screens/intro/intro.gd` (Era 1) y
`scenes/screens/intro_era7/intro_era7.gd` (Era 7) se hace un crossfade
`bg0 → bg1 → bg2 → bg3` de 2 segundos por paso. El último (`bg3`) queda
también como **fondo del nivel** durante la partida, por lo que tiene que
funcionar tanto como remate cinemático como telón de fondo permanente.

### Era 1 — Sótano, 2010

Locación común: pasillo o sala estrecha en un sótano olvidado. Suelo de
hormigón con cables enredados. A los lados, racks de servidores antiguos con
LEDs verdes y rojos parpadeando. Al fondo, una mesa con un monitor CRT.

#### `ui/level1/bg0.png` — paso 1 (descubrimiento)

Sala completamente a oscuras. El monitor CRT del fondo está **apagado**.
Solo un punto de luz tenue indica que el rack lateral sigue alimentado.

#### `ui/level1/bg1.png` — paso 2 (encendido)

Mismo encuadre. El CRT al fondo está **arrancando**: pantalla en gris o
con scanlines visibles, todavía sin contenido legible. La luz proyectada
empieza a iluminar débilmente la mesa.

#### `ui/level1/bg2.png` — paso 3 (despierto)

Mismo encuadre. El CRT muestra una **pantalla verde fósforo** con texto que
no se llega a leer (suficiente con que se intuya prompt de terminal). La
sala queda iluminada por ese tono verde.

#### `ui/level1/bg3.png` — paso 4 (estado estable, fondo de juego)

Mismo encuadre. CRT plenamente encendido, sala bañada en luz verde tenue.
Es la imagen que el jugador verá fijada durante toda la partida en Era 1:
no debe contener texto legible ni elementos animados llamativos, para no
distraer.

### Era 7 — Singularidad

Locación común: espacio sin suelo ni techo identificables. Concepto: la
materia ha dejado de importar. La progresión `bg0 → bg3` lleva del "casi
nada" al "explosión cognitiva".

#### `ui/level7/bg0.png` — paso 1

Vacío oscuro. Algunas chispas o puntos de luz dispersos sobre fondo
mayoritariamente negro. Atisbo de estructura de red apenas perceptible.

#### `ui/level7/bg1.png` — paso 2

Esos puntos se han organizado en una **red de nodos** parcial, con
conexiones tenues uniéndolos. Todavía hay zonas oscuras.

#### `ui/level7/bg2.png` — paso 3

La red ocupa la mayor parte del cuadro y vibra con datos circulando entre
nodos. Aparece un núcleo central más denso.

#### `ui/level7/bg3.png` — paso 4 (fondo de juego)

Estado pleno: red de nodos densamente conectada con un punto de luz central
del que irradian flujos hacia todas las direcciones. Como `level1/bg3`,
debe servir de telón fijo durante la partida — sin elementos que cambien
visualmente.

---

## 3. UI base

Marcos y elementos de interfaz que se reutilizan en todas las pantallas.
Todos llevan **rectángulo interior plano y limpio** donde el motor encaja
texto/contenido dinámicamente.

### `ui/button/button.png` — botón en reposo

Botón rectangular alargado. Borde decorativo perimetral. Zona interior
completamente plana (sin textura, sin gradiente fuerte) para que el texto
del botón se lea sobre ella.

### `ui/button/button-active.png` — botón pulsado / hover / focus

Mismo botón que el anterior pero con realce visual: la zona interior cambia
de tono o se le añade un highlight horizontal. Mismo tamaño exacto y misma
posición del rectángulo interior — los textos no deben moverse al cambiar
de estado.

### `ui/toggle/toggleON.png` — interruptor encendido

Switch horizontal con la palanca/punto desplazada hacia la derecha,
indicando estado activo.

### `ui/toggle/toggleOFF.png` — interruptor apagado

El mismo switch con la palanca a la izquierda, estado inactivo. Las dos
imágenes tienen las mismas dimensiones y la palanca cae a alturas idénticas.

### `ui/shop/icon-screen-shop.png` — marco del icono de mejora

Marco cuadrado decorativo. Tiene **dos puertos/conectores ornamentales en
el borde izquierdo** que sobresalen ligeramente. Rectángulo interior oscuro
y plano donde el motor coloca el icono PNG de la mejora correspondiente.

### `ui/shop/background-shop-text.png` — marco del bloque de texto de mejora

Marco rectangular ancho (≈3.5:1) con la misma familia visual que el marco
del icono — un puerto/conector decorativo a la izquierda, esquinas con
remates. Rectángulo interior plano donde se renderizan nombre,
descripción, efecto, coste y botón "COMPRAR".

### `ui/workstation/frame_era1.png` — marco del monitor en partida (Era 1)

Marco de un **monitor CRT antiguo** visto de frente: carcasa beige o gris,
botones físicos en el borde inferior, ventilaciones laterales. Pantalla
interior plana (un rectángulo oscuro de aspecto 16:10 aprox.) donde el
motor colocará dinámicamente los iconos de las mejoras compradas. **No
debe llevar contenido pintado dentro de la pantalla** — esa zona la
rellena el código.

### `ui/workstation/frame_era7.png` — marco del monitor en partida (Era 7)

Equivalente al anterior pero del periodo de la singularidad: un **panel
flotante / holográfico** sin carcasa física, con bordes que se diluyen.
Mismo aspecto de ventana interior (16:10) plana y oscura para que encaje
el contenido dinámico.

### `ui/level1/keyboard.png` — tap target de Era 1

Teclado mecánico antiguo (estilo IBM Model M / teclado beige de los 90),
vista 3/4 ortográfica. Carcasa oscura inclinada hacia el espectador con
4 filas de teclas claras escalonadas + barra espaciadora. Un cable
saliendo por la esquina superior derecha hacia fuera del cuadro. Es la
imagen sobre la que el jugador toca para "entrenar" en Era 1; se
renderiza **debajo de la workstation**, no superpuesta. Aspecto
**aproximadamente 2:1** (más ancho que alto, como un teclado real),
fondo transparente, sin sombra arrojada.

### `ui/cursor/cursor.png` — puntero del ratón (estado normal)

Puntero de flecha clásico de sistema operativo de finales de los 90 /
principios de 2000 (estilo Windows 98 / 2000), apuntando hacia la
**esquina superior izquierda**. Silueta blanca con borde negro de 1 px
para que se lea sobre cualquier fondo. Tamaño cuadrado pequeño
(suficiente para ser reconocible a 32 px sin perder forma); el **hotspot
está en la punta** (pixel superior izquierdo). Fondo transparente.

### `ui/cursor/cursor-pointer.png` — puntero del ratón (estado interactivo)

Mismo lenguaje visual que `cursor.png` pero con forma de **mano con
dedo índice extendido** (el cursor que el SO muestra al pasar sobre
enlaces o controles clicables). Mantiene el mismo tamaño cuadrado y el
mismo grosor de contorno negro. **Hotspot en la punta del dedo
índice**. Fondo transparente. Se usa cuando el ratón pasa sobre
elementos interactivos (botones, slots, tap target).

### `ui/workstation/slot_locked.png` — slot bloqueado

Icono de tamaño cuadrado que representa una mejora **aún no adquirida**.
Silueta neutra (un interrogante o una caja vacía). Lectura inmediata como
"hueco por desbloquear". Reutilizado por las 12 mejoras hasta que se
compran.

---

## 4. Identidad / marca

### `logotipo.png` — logotipo del juego

Logotipo de "THE TOKENIZER" sobre fondo opaco (cuadrado). Pensado para
splash / icono de aplicación.

### `logotipo-transparente.png` — logotipo recortado

Mismo logotipo, con **fondo transparente**. Lo usa `main_menu.tscn` sobre
el fondo del sótano.

### `home.png` — fondo de la pantalla home / main menu

Imagen vertical (proporciones móvil) que comparte locación con
`level1/bg3`: el sótano con el CRT central encendido. Sirve de telón sobre
el que se monta el logotipo y los botones del menú principal.

---

## 5. Mejoras de tienda — convenciones comunes

Las 12 mejoras (6 de Era 1, 6 de Era 7) comparten cuatro reglas de
composición para que la cuadrícula de la tienda lea como un set
coherente:

1. **Fondo neutro oscuro uniforme** (cercano a `#1a1c1e`), no
   transparente. Nada de degradados ni texturas en el fondo.
2. **Encuadre cuadrado**, sujeto centrado ocupando ~70-80% del recuadro
   con aire arriba y abajo.
3. **Vista 3/4 ortográfica** para objetos físicos de Era 1 (la
   misma inclinación para todos los iconos de la era).
4. **Sin sombra arrojada al suelo** — el objeto flota sobre el fondo
   plano. Solo modelado interno por bloques de píxel.

## 5.1 Mejoras de Era 1 (objetos físicos del sótano 2010)

| id `.tres`         | objeto / persona               | efecto       |
| ------------------ | ------------------------------ | ------------ |
| `era_1_intern`     | Becario en prácticas           | +1 / tap     |
| `era_1_coffee`     | Cafetera de oficina            | +2 / tap     |
| `era_1_floppy`     | Disquete con corpus            | x1.25 / tap  |
| `era_1_gpu`        | Tarjeta gráfica GeForce 2 MX   | +0.5 / s     |
| `era_1_cluster`    | Cluster Beowulf casero         | +1.5 / s     |
| `era_1_serverroom` | Sala de servidores refrigerada | x1.3 / s     |

### `ui/shop/upgrades/era_1_intern.png`

Becario de unos 20 años sentado en silla de oficina, vista 3/4. **El
monitor del escritorio aparece a su derecha** (no detrás), también en
3/4, mostrando que está trabajando. Camiseta lisa, pelo sin peinar,
postura ligeramente encorvada. Mano sobre el ratón.

### `ui/shop/upgrades/era_1_coffee.png`

Cafetera eléctrica de oficina (típica del 2000 con jarra de cristal,
asa de plástico negro y placa caliente debajo), llena de café oscuro.
Cuerpo de plástico negro mate, panel lateral con indicador. Vista 3/4.

### `ui/shop/upgrades/era_1_floppy.png`

Disquete de 3.5 pulgadas en posición ligeramente inclinada (vista
casi frontal). **Etiqueta escrita a mano** con texto corto poco
legible (estilo "CORPUS v3"). Carcasa azul oscura, pestaña metálica
de protección visible.

### `ui/shop/upgrades/era_1_gpu.png`

Tarjeta gráfica antigua (referencia: NVIDIA GeForce 2 MX), vista 3/4.
PCB verde visible, disipador pasivo metálico con ventilador pequeño,
chasis trasero con conectores VGA y DVI, conector AGP en el borde
inferior. La tarjeta apoyada sobre el fondo, sin chasis.

### `ui/shop/upgrades/era_1_cluster.png`

**Tres torres de PC viejas** (cajas beige y gris claro, con
disqueteras y unidades de CD frontales) apoyadas en grupo,
**frontales hacia el espectador**, conectadas entre sí por cables
ethernet azules y cables de alimentación grises.

### `ui/shop/upgrades/era_1_serverroom.png`

**Rack negro** de servidores rack-mount (al menos 4 unidades 1U
apiladas) con hileras de LEDs verdes y un parpadeo rojo aislado en
una unidad. **Ventilador grande de pie a la derecha** del rack,
empujando aire hacia él. Vista 3/4 del conjunto rack + ventilador.

---

## 6. Mejoras de tienda — Era 7

Las 6 mejoras de Era 7 encajan con la estética post-singularidad. Se
mantienen los `id`, nombres y efectos; se redescribe solo el **contenido
visual** de cada icono. Aplican las mismas 4 convenciones comunes del
§5 (fondo neutro oscuro, encuadre cuadrado, sin sombra) salvo que la
vista deja de ser 3/4 ortográfica para volverse **frontal abstracta**
(estética post-singularidad, sin materia identificable).

### `ui/shop/upgrades/era_7_rsi.png` — Recursive self-improvement

Un núcleo central del que **brotan copias de sí mismo en árbol**, cada
generación más detallada que la anterior. Idea visual: fractal que se
ramifica con consistencia.

### `ui/shop/upgrades/era_7_nas.png` — Neural architecture search

Un **grafo de red neuronal** cuyas conexiones se reorganizan: algunas se
deshacen mientras otras nuevas surgen. La forma global cambia, no es una
red estática.

### `ui/shop/upgrades/era_7_continual.png` — Continual learning

Cinta de datos entrando de forma continua por un lado de la red y siendo
absorbida en el núcleo. **Sin pausa visible** entre fase de entrenamiento e
inferencia.

### `ui/shop/upgrades/era_7_compute.png` — Síntesis de cómputo

Vista cenital de un **chip de silicio** con regiones diferenciadas (memoria,
lógica, interconexión) etiquetadas. Una de las regiones aún se está
"dibujando" — sugerencia de que el propio modelo está diseñando el silicio.

### `ui/shop/upgrades/era_7_distillation.png` — Auto-destilación

Dos esferas concéntricas: una **grande** difusa con muchos pesos, y otra
**pequeña** densa y nítida en el centro. Líneas de flujo van de la grande
hacia la pequeña, indicando compresión.

### `ui/shop/upgrades/era_7_hyperparams.png` — Conciencia de hiperparámetros

Tablero/dashboard flotante con **sliders y números** que se mueven por sí
solos. Una mano fantasmal (o simplemente "nadie") los está ajustando.

---

## 7. Cobertura

Quedan cubiertas las 38 imágenes PNG previstas en
`game/the-tokenizer/assets/sprites/`:

- 5 frames del investigador
- 4 fondos Era 1 + 4 fondos Era 7 + 1 teclado (tap target Era 1)
- 2 botones + 2 toggles + 2 cursores (normal y pointer)
- 2 marcos de tienda + 2 marcos de workstation + 1 slot bloqueado
- 2 logotipos + 1 home
- 6 iconos Era 1 + 6 iconos Era 7
