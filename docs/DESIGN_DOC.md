# RepLog — Design Doc

> Ver [`PRD.md`](PRD.md) para el qué/por qué de producto. Este documento cubre el cómo:
> arquitectura y decisiones de diseño técnico. Fuente: [`../gym-app-ideas.md`](../gym-app-ideas.md).

## Contexto

RepLog es una app nativa de Android (Kotlin + Jetpack Compose) para registrar entrenamientos de
gimnasio con fricción mínima. El repo hoy es solo el scaffold generado por Android Studio — no
existe código de dominio, persistencia ni navegación. Este documento existe para dejar por escrito
el diseño propuesto antes de escribir la primera línea de código de features, de forma que las
decisiones (modelo de datos, alcance del catálogo de ejercicios, almacenamiento) queden explícitas
en vez de implícitas en el código.

## Objetivo / No-objetivo

**Objetivo (MVP):** todo lo listado como must-have en [`PRD.md`](PRD.md#must-have-mvp) — registro
de ejercicios/series/rutinas con tipos de serie, historial, catálogo con sustitutos, notas por
ejercicio, import/export local, navegación por menú hamburguesa, Resumen/Reporte, Configuraciones
y temporizador de descanso.

**No-objetivo (por ahora):** todo lo listado como post-MVP en el PRD — gráficas, plantillas,
backup remoto, progresión automática. Explícitamente no se diseña para estos casos todavía; no se
debe pagar complejidad de arquitectura por adelantado para features que no están confirmadas.

## Diseño propuesto

### Modelo de datos (borrador)

- **Ejercicio**: nombre, grupo muscular, tipo de equipo (barra/mancuerna/máquina/peso corporal),
  tags libres, % de activación por grupo muscular (ej. press banca: pecho 70%, tríceps 20%,
  deltoide anterior 10%).
- **Ejercicio dentro de rutina**: no es lo mismo que "Ejercicio" — el esquema de series (cuántas
  series efectivas, reps objetivo, descanso) vive a este nivel porque puede variar por día dentro
  de la misma rutina (ej. "Elevaciones laterales": 4 series efectivas el martes, 3 el sábado).
- **Serie**: tipo (calentamiento / aproximación / efectiva), reps objetivo, descanso, y para
  aproximación el % de peso — configurable por tipo y potencialmente por etapa (una serie de
  aproximación puede tener varias etapas, cada una con su propio %, ej. sentadilla 50%×6 → 70%×4).
  El peso de calentamiento/aproximación se calcula como % del peso efectivo y se sugiere
  automáticamente. El peso efectivo sugerido toma el último registrado para ese ejercicio o su
  alternativa; si no hay historial previo (primer uso del ejercicio), el valor por defecto es
  **1 kg**. Cada serie se persiste en el historial al momento de registrarla, no al terminar la
  rutina — por eso navegar entre ejercicios del día (ver [Navegación](#navegación)) o interrumpir
  la sesión nunca pierde series ya registradas; no hace falta un modelo de datos nuevo de "sesión
  en curso". El campo de reps se inicializa con el valor objetivo definido en "Ejercicio dentro de
  rutina" y es ajustable con botones +/- al registrar (el ajuste no modifica el objetivo de la
  rutina, que solo cambia editándola). El avance entre tipos de serie (calentamiento → aproximación
  → efectiva) es **lineal**: no se puede saltear ni volver a un tipo ya completado. El peso
  efectivo es editable en cualquier punto del flujo (es la base de la que dependen las etapas
  anteriores); el peso sugerido de una serie de calentamiento/aproximación solo es editable
  **mientras esa etapa está activa** — al avanzar a la siguiente etapa queda fijo/de solo lectura,
  igual que el resto de esa etapa. No hay recálculo automático en cascada: cambiar el peso efectivo
  no reescribe pesos ya fijados de etapas pasadas.
- **Galería**: lista de medios (foto o video) asociada a un Ejercicio — análoga a "Nota" pero en
  formato visual. Empieza vacía; el usuario la va llenando con contenido de referencia (técnica,
  ajuste de máquina, etc.).
- **Rutina**: nombre, lista ordenada de "ejercicio dentro de rutina", posiblemente por día si es
  multi-día (ver [Rutina por defecto](#rutina-por-defecto)).
- **Historial**: por ejercicio, serie de puntos en el tiempo (peso, reps, tipo de serie) — el
  volumen/progreso reportado debe filtrar solo series efectivas.
- **Sustitutos**: relación muchos-a-muchos entre ejercicios equivalentes (mismo grupo
  muscular/patrón de movimiento), ej. Press banca con barra ↔ con mancuerna ↔ en máquina ↔
  Flexiones lastradas.
- **Nota**: texto libre asociado a un ejercicio (no a una sesión específica), ej. "ajustar asiento
  a nivel de barbilla".

### Rutina por defecto

El usuario ya tiene una rutina semanal en uso (documentada en
[`../rutina_semanal_optimizada.md`](../rutina_semanal_optimizada.md)) que debe precargarse como
rutina por defecto de la app. Tiene 5 días (Push, Pull, Legs, Full Upper, Brazos+Pierna+Pecho), por
lo que no es un PPL puro de 3 o 6 días pese a que el usuario la llama tentativamente así — vale la
pena renombrarla a algo más descriptivo o dejar que el usuario la nombre libremente al crearla en
la app. El usuario debe poder crear rutinas adicionales propias además de esta.

La rutina por defecto **no se puede eliminar** — la acción "Eliminar rutina" (ver
[Editar rutina](#editar-rutina)) queda deshabilitada u oculta para ella —, pero sí se puede editar
su contenido igual que cualquier otra rutina (días, ejercicios, esquema de series).

### Flujo "cambiar ejercicio"

El diferenciador clave del producto: durante el entrenamiento, un botón visible en la pantalla de
"ejercicio activo" abre la lista de sustitutos sugeridos y permite reemplazar el ejercicio en curso
sin salir de la pantalla ni perder el registro de la rutina. Ver el flujo completo en
[`DIAGRAMS.md`](DIAGRAMS.md#flujo-de-pantallas).

### Ejercicio activo (tabs)

La pantalla se organiza en 2 tabs:

- **Entrenamiento**: el contenido ya documentado (peso efectivo, tags, series por tipo con el
  flujo lineal, temporizador de descanso, notas, cambiar ejercicio).
- **Galería**: grilla de la entidad "Galería" del ejercicio activo (ver
  [Modelo de datos](#modelo-de-datos-borrador)); empieza vacía con una acción para agregar
  foto/video.

### Navegación

Patrón de navegación elegido: menú hamburguesa (nav drawer), ícono a la izquierda, accesible desde
**cualquier** pantalla de la app — tanto tocando el ícono como con gesto (swipe desde el borde
izquierdo) —, con 5 destinos de primer nivel:

- **Rutinas**: la pantalla "Home / Lista de rutinas" ya documentada — no es una pantalla nueva,
  solo su punto de entrada pasa a ser el menú.
- **Ejercicios**: catálogo completo de ejercicios, navegable de forma independiente (sin depender
  de estar dentro de una rutina). Reutiliza el mismo catálogo que alimenta la lista de sustitutos
  del flujo "cambiar ejercicio".
- **Resumen/Reporte**: ver subsección siguiente.
- **Herramientas**: por ahora contiene únicamente Import/Export de datos (ya documentado como
  must-have en el PRD; antes se accedía directo desde Home, ahora vive acá). Pensada como sección
  extensible para futuras utilidades.
- **Configuraciones**: ver subsección más abajo.

**Excepción — menú contextual en "Ejercicio activo":** dentro de esta pantalla, el mismo ícono
(izquierda) y gesto de apertura no muestran las 5 secciones globales, sino los ejercicios del día
de rutina en curso (ej. los de "Martes — Push"), cada uno marcado como hecho/actual/pendiente.
Tocar un ejercicio de esa lista
navega directo a él — permite saltar de orden sin perder el progreso ya registrado, porque cada
serie se persiste al registrarla (no al terminar la rutina; ver nota en
[Modelo de datos](#modelo-de-datos-borrador)). Al final de la lista hay un ítem "Terminar rutina?"
que pide confirmación ("¿Está seguro?") antes de cerrar la sesión y volver a Rutinas.

La librería de navegación concreta (Navigation Compose u otra) sigue sin definir — ver
[Preguntas abiertas](#preguntas-abiertas) — esto solo fija la estructura de alto nivel, no la
implementación.

### Editar rutina

Pantalla nueva, accedida desde "Detalle de rutina" (botón "Editar") o al crear una rutina desde
cero ("+ Nueva rutina" en Rutinas). Opera sobre las entidades "Rutina" y "Ejercicio dentro de
rutina" ya documentadas — no requiere modelo de datos nuevo:

- **Ejercicios dentro de un día**: agregar (desde el catálogo de Ejercicios), reordenar/mover, y
  eliminar.
- **Días** (las rutinas son multi-día, ver [Rutina por defecto](#rutina-por-defecto)): agregar,
  eliminar y renombrar.
- **Rutina completa**: crear (nueva rutina vacía, con nombre elegido por el usuario) y eliminar
  (con confirmación "¿Está seguro?") — **excepto la rutina por defecto**, que no se puede eliminar.

### Resumen/Reporte

Pantalla nueva, distinta del "Historial de ejercicio" ya documentado (ese se mantiene, accedido por
ejercicio individual). Resumen/Reporte agrega métricas **entre** ejercicios y rutinas: entrenamientos
completados, volumen total, racha de entrenamiento, etc. Usa el mismo almacenamiento de historial
que ya existe, pero con una agregación distinta (across-ejercicios en vez de por-ejercicio), por lo
que requiere lógica de dominio propia (ver diagrama de arquitectura en
[`DIAGRAMS.md`](DIAGRAMS.md#arquitectura)) — no un modelo de datos nuevo.

### Configuraciones

Preferencias de usuario, independientes de los datos de entrenamiento:

- **Tema**: claro / oscuro (posiblemente "seguir sistema").
- **Tamaño/densidad de la interfaz**: relevante porque la app se usa con el celular en la mano
  entre series.
- **Sistema de unidades**: métrico / imperial (afecta cómo se muestran y registran los pesos).

Esto implica una nueva pieza de almacenamiento local para preferencias, separada del storage de
datos de entrenamiento (rutinas/ejercicios/historial). El mecanismo concreto (DataStore,
SharedPreferences, etc.) es un detalle de implementación, no decidido acá.

### Temporizador de descanso

Se dispara automáticamente al registrar cualquier serie (calentamiento, aproximación o efectiva) en
la pantalla de "ejercicio activo". La duración de la cuenta regresiva es el campo "descanso" que ya
existe por tipo de serie a nivel de "ejercicio dentro de rutina" (ver [Modelo de
datos](#modelo-de-datos-borrador)) — no se agrega un campo nuevo, se reutiliza. Al llegar a cero,
avisa al usuario (visual y/o sonoro — el mecanismo exacto queda como detalle de implementación).

## Alternativas consideradas

Estas son alternativas reales sobre las que todavía no hay decisión tomada — están abiertas, no
descartadas (ver [Preguntas abiertas](#preguntas-abiertas)):

- **Almacenamiento local-only vs. cuenta de usuario con backend**: local-only reduce alcance y
  respeta "fricción mínima"; una cuenta habilita backup remoto pero agrega superficie (auth,
  sincronización, servidor). El backup remoto está marcado como opcional y ligado a esta decisión.
- **Catálogo de ejercicios/sustitutos curado a mano vs. fuente externa**: curado a mano da control
  total sobre calidad y relevancia de sustitutos pero no escala fácil; una fuente externa (API/DB
  pública de ejercicios) da cobertura amplia pero puede no tener el criterio de "sustituto
  equivalente" que necesita el flujo de swap.
- **% de activación muscular por ejercicio: estimación propia vs. fuente externa**: mismo trade-off
  que el catálogo — depende de si existe una fuente confiable y con la granularidad necesaria.

## Preguntas abiertas

- ¿Plataforma? (web app, móvil nativa, PWA) — el scaffold actual asume Android nativo, pero no está
  confirmado como decisión final documentada.
- ¿Cuenta de usuario / login, o todo local en el dispositivo?
- ¿Cómo se arma la base de datos de ejercicios comunes y sus alternativas? (curada a mano vs.
  fuente externa)
- ¿Nombre final del producto?
- ¿De dónde sale el % de activación muscular por ejercicio? (estimación propia/curada a mano vs.
  alguna fuente externa)
- Si hay backup remoto opcional, ¿qué proveedor/mecanismo se usaría? (Google Drive, servidor
  propio, otro)
