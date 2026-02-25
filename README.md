# Propuesta aplicaciones

- Sistema de gestión de casa inteligente para apertura de puertas. Integra inicio de sesión con Firebase Auth y guarda historial de aperturas y almacena el estado de las puertas en Cloud Firestore en la capa gratuita. **Ionic con React**

- Juego autoincremental con historia de los LLMs. Inicio de sesión y almacenamiento del estado del juego en la capa gratuita de Firebase con Cloud . Gestionaría la obtención de recursos en tiempo real, el mostrado de nuevos elementos a comprar con sus imágenes, sonidos para alguno de los elementos y mecánicas del giróscopo para un minijuego dentro del juego. **Flutter**

- Afinador de guitarra. Para afinar una guitarra el mejor algoritmo de afinación es el [algoritmo YIN](http://audition.ens.fr/adc/pdf/2002_JASA_YIN.pdf). Existe una librería ya implementada en Kotlin llamada [TarsosDSP](https://github.com/JorenSix/TarsosDSP) que implementa el algoritmo YIN, habría que integrarla y mostrar todo con una UI sencilla donde se autodetecte la cuerda que se está tocando y se estimen los tonos por encima o por debajo del valor objetivo. **Kotlin**