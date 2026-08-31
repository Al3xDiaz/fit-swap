# RepLog — Product Requirements Document

> Nombre provisional: **RepLog**. Alternativas: FitSwap, Rutina+, IronLog, SetLog.
> Fuente: [`../gym-app-ideas.md`](../gym-app-ideas.md).

## Problema

Llevar el registro de entrenamiento en notas sueltas o papel es incómodo y hace que se pierda el
historial. Las apps grandes (Strong, Hevy, etc.) tienen demasiadas funciones para lo que se busca:
algo simple y enfocado. Además, cuando una máquina o barra está ocupada en el gimnasio, no hay
forma rápida de saber qué ejercicio alternativo hacer sin salir de la app o perder el registro de
la rutina en curso.

## Usuarios objetivo

Una persona que entrena en el gimnasio con el celular en la mano, entre series — necesita registrar
peso/reps con fricción mínima, y necesita poder resolver sobre la marcha "esta máquina está
ocupada, ¿qué hago en su lugar?" sin romper el flujo de la rutina ni perder el registro.

## Requisitos

### Must-have (MVP)

- **Registro de ejercicios**: nombre, grupo muscular, tipo de equipo (barra, mancuerna, máquina,
  peso corporal), tags libres, % de activación por grupo muscular.
- **Registro de series con tipos**: calentamiento, aproximación, efectiva — cada una con reps
  objetivo, descanso y (aproximación) % de peso configurables, y pudiendo variar por día dentro de
  la misma rutina. Aproximación admite varias etapas (ej. 50%×6 → 70%×4). El peso efectivo nunca
  empieza vacío: toma el último registrado para ese ejercicio o su alternativa, o **1 kg** si no
  hay historial previo. La pantalla de "ejercicio activo" también muestra los tags del ejercicio
  (grupo muscular, equipo, tags libres) en una sección visible. El campo de reps se pre-llena con
  el objetivo de la rutina y es ajustable con botones +/-. El paso entre tipos de serie
  (calentamiento → aproximación → efectiva) es lineal — no se puede saltear ni retroceder de tipo.
  El peso efectivo es editable en cualquier momento del flujo; el peso sugerido de calentamiento/
  aproximación solo es editable mientras esa etapa está activa (se fija al avanzar de etapa, sin
  recálculo automático en cascada).
- **Galería del ejercicio**: la pantalla de "ejercicio activo" tiene 2 tabs — la de entrenamiento
  (descrita arriba) y una galería personalizable por ejercicio (fotos/videos de referencia), vacía
  por defecto.
- **Temporizador de descanso**: al registrar una serie (calentamiento, aproximación o efectiva) se
  inicia automáticamente una cuenta regresiva con la duración de descanso configurada para ese tipo
  de serie, con aviso al llegar a cero.
- **Navegación (menú hamburguesa)**: ícono a la izquierda, disponible en todas las pantallas, y el
  drawer también se puede abrir con gesto (swipe desde el borde izquierdo). Da acceso a 5
  secciones —
  Rutinas, Ejercicios (catálogo completo, independiente de una rutina), Resumen/Reporte (dashboard
  agregado, ver más abajo), Herramientas (import/export de datos, extensible a futuro) y
  Configuraciones (tema claro/oscuro, tamaño/densidad de UI, sistema de unidades
  métrico/imperial) — excepto en "Ejercicio activo", donde el menú es contextual: muestra los
  ejercicios del día de rutina en curso (navegable, sin perder el progreso de la sesión al saltar
  entre ellos) y un ítem "Terminar rutina?" con confirmación ("¿Está seguro?").
- **Resumen/Reporte**: pantalla de métricas agregadas entre ejercicios y rutinas (entrenamientos
  completados, volumen total, racha, etc.), separada del Historial por ejercicio existente.
- **Rutinas**: agrupar ejercicios con orden, series objetivo por tipo y descanso; debe existir una
  rutina por defecto (ver [Rutina por defecto](DESIGN_DOC.md#rutina-por-defecto)) y permitir crear
  rutinas adicionales. El usuario puede editar cualquier rutina (agregar/mover/eliminar ejercicios
  dentro de un día, agregar/eliminar/renombrar días) y eliminar rutinas completas — excepto la
  rutina por defecto, que no se puede eliminar (sí editar su contenido).
- **Historial**: progreso por ejercicio en el tiempo (peso máximo, volumen, últimas sesiones),
  separado por tipo de serie — solo las efectivas cuentan como volumen real.
- **Base de ejercicios por defecto**: catálogo precargado con ejercicios comunes, incluyendo un
  ejercicio base de calentamiento/cardio ("Caminar en cinta" como primera alternativa).
- **Ejercicios alternativos**: cada ejercicio tiene una lista de sustitutos equivalentes
  (mismo grupo muscular/patrón de movimiento); el cambio debe poder hacerse *durante* el
  entrenamiento, no solo al planificar.
- **Notas por ejercicio**: comentarios/notas técnicas accesibles sin salir de la pantalla de
  entrenamiento.
- **Import/export de datos**: exportar/importar el registro completo a un archivo local
  (JSON/CSV). Backup remoto es opcional y solo se activa si el usuario lo decide explícitamente.

### Nice-to-have (post-MVP)

- Gráficas de progreso (peso máximo, volumen semanal), posiblemente desglosadas por grupo muscular.
- Plantillas de rutinas predefinidas (push/pull/legs, full body, etc.).
- Backup remoto (proveedor a definir: Drive, servidor propio, etc.).
- Sugerencias de progresión automática (ej. "subiste 3 veces seguidas a 5 reps, probá +2.5kg").
- Modo offline-first explícito (uso en el gimnasio sin buena señal).

## Métricas de éxito

No hay datos de uso todavía (proyecto sin código de features); estas son métricas **propuestas**
para validar una vez que exista una versión usable, no cifras objetivo actuales:

- Tiempo promedio para registrar una serie (proxy de "fricción mínima" — el objetivo declarado del
  producto).
- Tasa de uso del flujo "cambiar ejercicio" a mitad de entrenamiento (valida el diferenciador
  clave del producto).
- Retención semanal del usuario (¿se sigue usando en vez de volver a notas sueltas/papel?).
- % de sesiones registradas sin necesidad de salir de la pantalla de entrenamiento (proxy de que el
  acceso a notas/alternativas realmente evita romper el flujo).

## Fuera de alcance (MVP)

- Cuentas de usuario / login remoto — todo local en el dispositivo salvo que se decida lo
  contrario (ver [pregunta abierta](DESIGN_DOC.md#preguntas-abiertas)).
- Backup remoto automático.
- Cualquier ítem listado como "Ideas futuras" en `gym-app-ideas.md` (gráficas, plantillas,
  sugerencias de progresión automática).
- Funciones sociales, comparación entre usuarios, coaching remoto — no mencionadas en las notas de
  producto, no forman parte de la visión actual.
