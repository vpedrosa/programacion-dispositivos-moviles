# Programación de Dispositivos Móviles

[![Smart Home CI](https://github.com/vpedrosa/programacion-dispositivos-moviles/actions/workflows/smart-home-ci.yml/badge.svg)](https://github.com/vpedrosa/programacion-dispositivos-moviles/actions/workflows/smart-home-ci.yml)

*Valentín Pedrosa Campoy*

## Índice

- [Smart Home](#smart-home)
- [The Tokenizer](#the-tokenizer)
- [City Defender](#city-defender)

---

## Smart Home

REGISTRAR DISPOSITIVO + PROCESO SIMULADO: escaneo de QR de la lista. Añadir nombre del dispositivo. Botón en encender pensando. Cerraduras -> Apagar todas -> Abrir todas. Iconos encendido apagado. En el termostato tengo más botones de la cuenta. Temperatura actual / objetivo -> La actual más grande. Notificación de temperatura sólo si sube o baja mucho. Historial de acciones. Generación aleatoria + duración de lo que tengas las bombillas. Dos inputs de hora de inicio y hora de fin. Luces + persianas.

Aplicación multiplataforma desarrollada con **Kotlin Multiplatform** para la gestión de una casa inteligente. Utiliza el protocolo Matter con dispositivos simulados, permitiendo el control individual, por grupos lógicos (habitaciones) y por categorías de dispositivos. Incluye módulo para Wear OS (exclusivamente por voz), control por voz en la app principal y un sistema de notificaciones basado en eventos de sensores.

Contará con internacionalización y estará localizada en inglés y español.

**[Ver prototipo visual](smart-home/PROTOTIPO.md)** | **[Integración Matter SDK](smart-home/MATTER.md)** | **[Simulación matter.js](smart-home/simulation/README.md)**

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

**[Ver prototipo visual Wear OS](smart-home-wear-os/PROTOTIPO.md)**

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

## City Defender

Implementación moderna del clásico arcade *Missile Command* desarrollada en **Godot**. El jugador defiende 4 ciudades de oleadas de misiles enemigos que caen desde la parte superior de la pantalla, tocando para lanzar misiles interceptores que explotan en el punto de contacto y destruyen los misiles enemigos dentro de su radio de explosión. Si las 4 ciudades son destruidas, la partida termina.

### Mecánicas de Juego

#### Defensa y Ciudades

- **Misiles interceptores:** El jugador toca la pantalla para lanzar un misil desde la base de defensa central. Al llegar al punto indicado, explota con un radio de área que destruye los misiles enemigos cercanos. Existe un cooldown corto entre disparos.
- **Ciudades:** 4 ciudades con barra de salud individual. Cuando un misil enemigo impacta una ciudad, pierde vida. A 0 HP la ciudad queda destruida.

#### Misiles Enemigos

- **Misiles normales:** Caen desde posiciones aleatorias hacia una ciudad aleatoria en línea recta.
- **Misiles rápidos:** Aparecen conforme avanza la partida, con el doble de velocidad y apariencia diferenciada.
- **Misiles pesados:** Aparecen en fases avanzadas y requieren dos impactos para ser destruidos.

#### Dificultad Progresiva

La dificultad aumenta de forma continua conforme avanza la partida: la velocidad y frecuencia de los misiles enemigos se incrementan gradualmente, se introducen tipos de misiles más peligrosos y periódicamente aparecen oleadas especiales con ráfagas de múltiples misiles simultáneos.

#### Sistema de Puntuación (Score)

El jugador acumula un **score** que aumenta con el tiempo de supervivencia y con cada misil enemigo destruido. Los misiles más difíciles otorgan más puntos, y destruir varios misiles con una sola explosión concede un bonus. Este score es el que se registra en el ranking global al finalizar la partida.

#### Sistema de Dinero y Power-ups

Cada misil destruido otorga dinero (más dinero cuanto más difícil sea el misil, con bonus por destrucciones múltiples en una sola explosión). El dinero se gasta en una tienda accesible durante la partida que pausa el juego:

| Power-up | Efecto |
| --- | --- |
| **Reparar ciudad** | Restaura vida a una ciudad dañada |
| **Reconstruir ciudad** | Revive una ciudad destruida con vida mínima |
| **Escudo temporal** | Escudo sobre todas las ciudades vivas que absorbe un impacto por ciudad |
| **Radio de explosión+** | Aumenta permanentemente el radio de explosión de los misiles interceptores (acumulable) |
| **Modo Gatling** | Mejora permanente: mantén pulsado para disparar en ráfaga continua sin soltar |
| **Bomba EMP** | Destruye todos los misiles enemigos en pantalla (uso único) |
| **Cadencia+** | Reduce permanentemente el cooldown entre disparos (acumulable) |
| **Velocidad de giro** | Aumenta permanentemente la velocidad de rotación de la torreta |

#### Highscores con Firebase Firestore

Al terminar la partida, la puntuación se envía automáticamente y de forma anónima a Firebase Firestore. El ranking de los 10 mejores scores (puntuación y fecha) es accesible desde el menú principal.

### Pantallas de la Aplicación

- **Menú Principal:** Botones de jugar, ver highscores, ajustes y salir.
- **Pantalla de Juego:** Gameplay con HUD mostrando score, dinero, vida de las ciudades, botón de acceso a la tienda y botón de ajustes.
- **Tienda:** Overlay con los power-ups disponibles y sus costes. Pausa el juego mientras está abierta.
- **Game Over:** Puntuación final con envío automático al ranking global y botones para ver highscores, reintentar o volver al menú.
- **Highscores:** Tabla con el top 10 global anónimo (puntuación y fecha) obtenido de Firestore.
