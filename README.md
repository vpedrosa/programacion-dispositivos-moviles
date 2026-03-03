# Programación de Dispositivos Móviles

*Valentín Pedrosa Campoy*

## Índice

- [Smart Home](#smart-home)
- [The Tokenizer](#the-tokenizer)
- [Guitar Pal (Pendiente de reevaluación)](#guitar-pal-pendiente-de-reevaluación)

---

## Smart Home

Aplicación multiplataforma desarrollada con **Kotlin Multiplatform** para la gestión de una casa inteligente. Utiliza el protocolo Matter con dispositivos simulados, permitiendo el control individual, por grupos lógicos (habitaciones) y por categorías de dispositivos. Incluye módulo para Wear OS (exclusivamente por voz), control por voz en la app principal y un sistema de notificaciones basado en eventos de sensores.

Contará con internacionalización y estará localizada en inglés y español.

**[Ver prototipo visual](smart-home/PROTOTIPO.md)**

> **Nota sobre Wear OS:** El módulo Wear OS únicamente captura el audio del usuario y lo envía a la aplicación principal. Es la app principal la que realiza el procesamiento speech-to-text y ejecuta el control mediante comandos de voz.

### Dispositivos Simulados (27 en total)

| Categoría | Ud | Detalle |
| :--- | :---: | --- |
| Cerraduras | 2 | Puerta de entrada + puerta de garaje. Acciones: apertura/cierre |
| Sensor de contacto | 1 | En la puerta de entrada (detecta abierta/cerrada) |
| Bombillas inteligentes | 10 | Con control de color y nivel de intensidad |
| Interruptores on/off | 5 | Interruptores inteligentes simples |
| Smart TV + Chromecast | 1 | Con capacidad de casting (reproducción de vídeo) |
| Persianas | 4 | Con control de apertura/cierre (y niveles) |
| Sensor de humos | 1 | Emite eventos/alertas |
| Sensor de fugas de agua | 1 | Emite eventos/alertas |
| Sensor de temperatura | 1 | Emite lecturas periódicas |
| Termostato | 1 | Emite eventos y permite ajuste de temperatura |

### Funcionalidades Principales

#### Gestión por Grupos Lógicos (Habitaciones)

Permite crear agrupaciones lógicas de dispositivos asociadas a estancias de la casa (salón, dormitorio, cocina, garaje, etc.) y ejecutar acciones conjuntas sobre todos los dispositivos de un grupo. Cada grupo puede personalizarse con una **fotografía** representativa de la estancia, tomada desde la cámara del dispositivo o seleccionada de la galería.

#### Gestión de Dispositivos

Vista unificada de todos los dispositivos de la casa, agrupados por tipo (bombillas, cerraduras, persianas, interruptores, sensores, termostato, Smart TV). Permite el control individual de cada dispositivo y acciones conjuntas por categoría (por ejemplo: apagar todas las bombillas, cerrar todas las persianas).

#### Recepción de Eventos y Notificaciones

Los dispositivos que emiten eventos (termostato, sensores de humo, agua, contacto, temperatura) generan notificaciones push en el dispositivo. Incluye una notificación automática si la puerta de entrada permanece abierta.

#### Control por Voz

Actuación sobre los dispositivos mediante comandos de voz (encender/apagar luces, abrir/cerrar persianas, etc.). Disponible tanto en la app principal como desde el módulo Wear OS.

#### Módulo Wear OS (Solo Voz)

Versión para smartwatches con Wear OS que permite controlar los dispositivos de la casa **exclusivamente mediante comandos de voz**. El reloj captura el audio y lo envía a la aplicación principal, que se encarga del reconocimiento de voz (speech-to-text) y de ejecutar las acciones correspondientes. La interfaz se centra en la escucha y confirmación de órdenes por voz, sin controles táctiles para actuar sobre dispositivos.

#### Modo Antiokupas

Funcionalidad de simulación de presencia: enciende luces y reproduce un vídeo de YouTube en la Smart TV siguiendo un patrón horario programado, simulando que la casa está habitada.

### Pantallas de la Aplicación

#### App Principal (Android)

- **Pantalla de Inicio / Dashboard:** Vista general del estado de la casa: resumen de dispositivos activos, alertas recientes, accesos directos a grupos y dispositivos.
- **Pantalla de Grupos Lógicos (Habitaciones):** Lista de grupos con su fotografía asociada y nombre. Opción de crear nuevo grupo, asignar dispositivos y añadir/cambiar la foto (cámara o galería). Al seleccionar un grupo: vista de sus dispositivos con controles individuales y acción conjunta.
- **Pantalla de Edición de Grupo:** Nombre del grupo, selección de dispositivos asignados. Captura o selección de fotografía para el grupo (integración con cámara y galería del dispositivo).
- **Pantalla de Dispositivos:** Lista unificada de todos los dispositivos agrupados por tipo (bombillas, cerraduras, persianas, interruptores, sensores, termostato, Smart TV). Cada sección permite control individual y acción conjunta. Al seleccionar un dispositivo se accede a su detalle.
- **Pantalla de Detalle de Dispositivo:** Control específico según tipo: bombilla (color picker + slider de intensidad), cerradura (abrir/cerrar), persiana (nivel de apertura), termostato (ajuste de temperatura objetivo), TV (casting/reproducción), interruptor (on/off).
- **Pantalla de Notificaciones / Eventos:** Historial de eventos recibidos: alertas de humo, fugas de agua, puerta abierta, lecturas de temperatura, cambios del termostato.
- **Pantalla de Configuración del Modo Antiokupas:** Activación/desactivación del modo. Configuración del patrón horario (franjas de encendido de luces y reproducción de vídeo). Selección del vídeo de YouTube a reproducir.
- **Pantalla de Control por Voz:** Interfaz para activar el reconocimiento de voz y enviar comandos a los dispositivos (o integrada como botón flotante accesible desde cualquier pantalla).
- **Pantalla de Configuración / Ajustes:** Gestión de dispositivos simulados (comisionamiento Matter). Preferencias de notificaciones.

#### App Wear OS (Smartwatch)

- **Pantalla Principal Wear OS:** Interfaz mínima con botón de activación de voz y estado de escucha/confirmación. Muestra feedback visual del comando reconocido y resultado de la acción ejecutada. El audio se envía a la app principal para su procesamiento.

---

## The Tokenizer

Juego idle/clicker [autoincremental](https://es.wikipedia.org/wiki/Videojuego_incremental) desarrollado en **Godot** ambientado en la historia real de los modelos de lenguaje (LLMs). El jugador encarna a un investigador anónimo que en 2010 descubre un mensaje profético en un servidor abandonado y emprende un viaje obsesivo por entrenar modelos cada vez más potentes hasta alcanzar la AGI. El juego combina mecánicas de generación pasiva de recursos, compra de mejoras, minijuegos interactivos con sensores del dispositivo (acelerómetro, giroscopio, multitouch) y un sistema de decisiones éticas que determina el final.

### Narrativa y Estructura por Eras

El juego se divide en **7 eras** basadas en hitos reales de la historia de los LLMs. Cada era tiene su propia estética visual, música ambiental, mejoras comprables y un **jefe** (hito técnico) que el jugador debe superar para avanzar.

| Era | Nombre | Período | Tecnología clave |
| --- | --- | --- | --- |
| 1 | El Sótano | 2010-2013 | N-grams, Word2Vec |
| 2 | La Chispa Recurrente | 2014-2016 | RNNs, LSTMs |
| 3 | Attention Is All You Need | 2017-2018 | Transformers, BPE |
| 4 | La Carrera de los Gigantes | 2019-2020 | GPT-2, BERT, Fine-tuning |
| 5 | El Modelo Habla | 2021-2022 | RLHF, Chain-of-Thought |
| 6 | La Guerra de los Modelos | 2023-2025 | Multimodalidad, MoE |
| 7 | Singularidad | 2026 - ??? | Auto-mejora |

> **Alcance de la práctica:** Para no añadir demasiada complejidad, únicamente se implementarán la **Era 1 (El Sótano)** y la **Era 7 (Singularidad)**. El resto de eras se documentan aquí como parte del diseño completo del juego, pero quedan fuera del alcance de esta práctica.

*(Los jefes concretos de cada era quedan por definir, pero son hitos técnicos que exigen alcanzar cierto nivel de recursos/mejoras.)*

### Mecánicas de Juego

#### Generación de Recursos (Tokens)

- **Era 1:** Generación manual mediante tap en pantalla (clicker puro).
- **Eras 2-7:** Generación pasiva/automática que se incrementa con las mejoras adquiridas.

#### Sistema de Mejoras

Compra de mejoras con los tokens generados que incrementan la producción de recursos y desbloquean el avance entre eras. Cada era tiene su propio catálogo de mejoras temáticas acorde a la tecnología de la época.

#### Sistema de Decisiones Éticas y Finales Múltiples

A lo largo de las eras aparecen eventos predefinidos que presentan dilemas éticos al jugador. Las decisiones acumuladas determinan uno de 3 finales diferentes en la Era 7:

- **Final Responsable:** Mayoría de decisiones éticas responsables.
- **Final Cuestionable:** Mayoría de decisiones éticamente cuestionables.
- **Final Equilibrado:** Mezcla de decisiones sin predominancia clara.

#### Minijuegos (Activación Aleatoria)

Se activan de forma aleatoria durante la partida y otorgan bonificaciones o penalizaciones:

- **Backpropagation:** Aparece una red neuronal 2D con pesos mal calibrados (marcados en rojo). El jugador debe corregirlos simultáneamente usando multitouch y gestos táctiles (pellizcar, separar) para ajustar los valores.
- **Refrigeración:** El jugador debe agitar el teléfono rítmicamente simulando el bombeo de energía a los sistemas de refrigeración. Usa el acelerómetro y giroscopio. Si el ritmo es demasiado rápido o lento, las GPUs se sobrecalientan y dejan de producir. Si es correcto, se obtiene una bonificación temporal de rendimiento.

> **Nota:** El minijuego de **Exploración del Espacio Latente** (basado en magnetómetro) no se implementará para no añadir demasiada complejidad a la práctica.

#### Mecánica de Realidad Cuántica (New Game+)

A partir de cierto punto del juego, se puede activar un evento en el que el modelo desvela los secretos de la cuántica y genera una realidad paralela. Esto reinicia el juego pero el jugador acumula **qubits**, un recurso especial que solo se obtiene de esta forma y que:

- Habilita multiplicadores adicionales sobre la generación de recursos.
- Facilita superar los jefes de las eras en partidas sucesivas.
- Permite al jugador rejugar para descubrir los 3 finales diferentes.

#### Guardado Automático

El estado del juego se guarda automáticamente cada **30 segundos**.

#### Multiplicador de Evaluación (Modo Debug)

Multiplicador muy elevado activable/desactivable desde la configuración que permite avanzar rápidamente entre eras, pensado para facilitar la evaluación académica del juego.

### Pantallas de la Aplicación

- **Pantalla de Inicio / Menú Principal:** Botón de nueva partida, continuar partida, configuración. Ambientación visual acorde al tono narrativo (servidor abandonado, estética retro).
- **Pantalla de Introducción Narrativa:** Secuencia cinemática o textual: descubrimiento del servidor en el sótano, lectura del mensaje profético, arranque del viaje.
- **Pantalla Principal de Juego (HUD por Era):** Zona central de interacción (tap en Era 1, visualización de generación pasiva en el resto). Contador de tokens/recursos actuales. Indicador de era actual con estética visual y música ambiental propias de cada era. Barra o indicador de progreso hacia el jefe de la era. Acceso a la tienda de mejoras, eventos y minijuegos cuando se activan. Contador de qubits (si aplica, tras desbloquear la mecánica cuántica).
- **Pantalla de Tienda de Mejoras:** Catálogo de mejoras disponibles en la era actual. Coste en tokens, descripción del efecto y estado (comprada o no). Sección de multiplicadores de qubits (cuando están desbloqueados).
- **Pantalla de Evento Ético / Decisión:** Presentación narrativa del dilema ético. Opciones de decisión con sus implicaciones implícitas. Feedback visual tras la elección.
- **Pantalla de Minijuego: Backpropagation:** Red neuronal 2D con nodos y pesos visualizados. Interfaz multitouch para ajustar pesos simultáneamente mediante gestos.
- **Pantalla de Minijuego: Refrigeración:** Visualización de servidores/GPUs con indicador de temperatura. Feedback en tiempo real del ritmo de agitación (acelerómetro/giroscopio). Indicador de zona óptima de ritmo.
- **Pantalla de Jefe de Era:** Enfrentamiento con el hito técnico de la era. Visualización del progreso y recursos necesarios para superarlo.
- **Pantalla de Evento Cuántico / Reinicio:** Narrativa del descubrimiento cuántico. Confirmación de reinicio a realidad paralela. Resumen de qubits acumulados y multiplicadores habilitados.
- **Pantalla de Final:** Una de las 3 variantes narrativas según las decisiones éticas tomadas. Resumen de la partida (decisiones, eras completadas, qubits).
- **Pantalla de Configuración / Ajustes:** Activar/desactivar el multiplicador de evaluación (modo debug). Ajustes de sonido y música. Gestión de guardado.

---

## Guitar Pal (Pendiente de reevaluación)

> **Estado: Pendiente de reevaluar.** Esta tercera aplicación está actualmente en evaluación para determinar si se incluirá en el alcance de la práctica. La información a continuación describe el diseño propuesto.

Aplicación nativa Android desarrollada en Kotlin para músicos de instrumentos de cuerda. Integra un afinador basado en el algoritmo YIN (mediante la librería TarsosDSP), un metrónomo configurable, un generador de tonos de referencia, grabación y análisis de sesiones de práctica, y soporte para múltiples instrumentos con afinaciones alternativas y perfiles personalizados. Incluye un módulo Wear OS con metrónomo háptico.

Contará con internacionalización y estará localizada en inglés y español.

### Instrumentos Soportados

| Instrumento | Afinación estándar | Ejemplos de afinaciones alternativas |
| :--- | --- | :--- |
| Guitarra (6 cuerdas) | E A D G B E | Drop D, DADGAD, Open G, Open D, Half Step Down |
| Bajo eléctrico (4/5 cuerdas) | E A D G (B E A D G) | Drop D, Half Step Down, D Standard |
| Ukelele | G C E A | D Tuning, Baritone (D G B E) |

### Funcionalidades Principales

- **Afinador con Autodetección:** Detección de pitch en tiempo real mediante el algoritmo YIN (TarsosDSP). Autodetecta la cuerda que se está tocando, muestra la nota detectada y la desviación respecto a la frecuencia objetivo.
- **Perfiles de Afinación:** Sistema de perfiles persistentes donde el usuario puede seleccionar instrumento, elegir una afinación predefinida o crear afinaciones personalizadas.
- **Generador de Tonos de Referencia:** Síntesis de audio que reproduce la frecuencia exacta de cada cuerda según el instrumento y afinación seleccionados.
- **Metrónomo Configurable:** Metrónomo con ajuste de BPM, selección de compás y configuración de acentos.
- **Grabación y Análisis de Sesiones de Práctica:** Grabación de audio con análisis posterior de precisión de afinación, notas más tocadas, estabilidad tonal y evolución entre sesiones.
- **Módulo Wear OS - Metrónomo Háptico:** Metrónomo basado en vibración háptica para práctica silenciosa. Control de BPM desde el reloj.

### Pantallas de la Aplicación

#### Módulo principal para Android

- **Pantalla Principal / Hub:** Acceso directo a afinador, metrónomo, grabadora y generador de tonos. Indicador del perfil de instrumento/afinación activo.
- **Pantalla de Afinador:** Selector de instrumento y afinación, nota detectada en tiempo real, indicador de desviación tonal, autodetección de cuerda.
- **Pantalla de Generador de Tonos de Referencia:** Representación de las cuerdas del instrumento, botón por cuerda para reproducir su tono, frecuencia objetivo visible.
- **Pantalla de Metrónomo:** Control de BPM (slider + entrada numérica + tap tempo), selector de compás y acentos, visualización rítmica.
- **Pantalla de Grabación de Sesión:** Controles de grabación, visualización en tiempo real del audio capturado.
- **Pantalla de Análisis de Sesión:** Historial de sesiones, gráficas de precisión, notas frecuentes, estabilidad tonal, comparativa entre sesiones.
- **Pantalla de Gestión de Perfiles de Afinación:** Lista de perfiles guardados, creación/edición/eliminación de perfiles personalizados, afinaciones predefinidas por instrumento.
- **Pantalla de Configuración / Ajustes:** Sensibilidad del micrófono, frecuencia de referencia A4, gestión de datos de sesiones.

#### App Wear OS

- **Pantalla de Metrónomo Háptico:** Control de BPM, inicio/parada, vibración háptica marcando el tempo.
