# RepLog — Ideas del proyecto

> Nombre provisional: **RepLog**. Alternativas: FitSwap, Rutina+, IronLog, SetLog.

## 1. Concepto

App simple para llevar registro de entrenamiento en el gimnasio: ejercicios, rutinas y pesos. Enfoque en fricción mínima durante el entrenamiento (se usa con el celular en la mano, entre series).

## 2. Problema que resuelve

- Llevar el registro en notas sueltas o papel es incómodo y se pierde el historial.
- Las apps grandes (Strong, Hevy, etc.) tienen demasiadas funciones; se busca algo simple y enfocado.
- Cuando una máquina/barra está ocupada, no hay forma rápida de saber qué ejercicio alternativo hacer sin salir de la app o perder el registro de la rutina.

## 3. Funcionalidad core (MVP)

- [ ] **Registro de ejercicios**: nombre, grupo muscular, tipo de equipo (barra, mancuerna, máquina, peso corporal), tags libres (ej. "straps"), y % de activación por grupo muscular (ej. press banca: pecho 70%, tríceps 20%, deltoide anterior 10%) — ver pregunta abierta sobre el origen de estos datos.
- [ ] **Registro de series con tipos de serie**: cada serie es de tipo **calentamiento**, **aproximación** o **efectiva**.
  - Reps objetivo, descanso y (para aproximación) el % de peso son configurables por tipo de serie, y pueden variar según el día dentro de una misma rutina: el mismo ejercicio puede tener distinto número de series según el día (ej. "Elevaciones laterales" tiene 4 series efectivas el martes pero 3 el sábado), así que este esquema vive a nivel de "ejercicio dentro de la rutina", no como propiedad global del ejercicio.
  - Una serie de aproximación puede tener varias etapas, cada una con su propio % (ej. sentadilla: 50% × 6 → 70% × 4).
  - El peso de calentamiento/aproximación se define como % del peso de la serie efectiva, y la app sugiere el peso resultante automáticamente durante el entrenamiento.
  - El peso de la serie efectiva es el que se muestra prominente en pantalla mientras se trabaja el ejercicio, y **nunca empieza vacío**: por defecto toma el último peso efectivo registrado para ese ejercicio (o su alternativa, si se hizo swap). Si no hay historial previo (primera vez que se hace el ejercicio), arranca en **1 kg**.
  - La pantalla de "ejercicio activo" también muestra los tags del ejercicio (grupo muscular, equipo, tags libres) en una sección visible, sin necesidad de salir a la pantalla de Ejercicios.
  - El campo de reps se pre-llena con el objetivo configurado en la rutina, pero es ajustable hacia arriba o abajo con botones +/- si no se logra (o se supera) esa cantidad durante la serie; el ajuste no modifica el objetivo de la rutina, que solo cambia editándola.
  - El paso entre tipos de serie (calentamiento → aproximación → efectiva) es lineal: no se puede saltear ni retroceder a un tipo anterior. El peso efectivo se puede editar en cualquier momento del flujo (es la base de la que dependen calentamiento/aproximación), pero el peso de una serie de calentamiento/aproximación solo se puede editar mientras esa etapa está activa — al pasar a la siguiente etapa queda fijo, sin recálculo automático en cascada entre etapas.
- [ ] **Temporizador de descanso**: al terminar de registrar una serie (calentamiento, aproximación o efectiva) arranca automáticamente una cuenta regresiva con el tiempo de descanso configurado para ese tipo de serie (reutiliza el campo "descanso" ya definido por tipo de serie a nivel de rutina), con aviso al llegar a cero.
- [ ] **Navegación por menú hamburguesa**: ícono a la izquierda, disponible en **todas** las pantallas; el drawer también se puede abrir con gesto (swipe desde el borde izquierdo), no solo tocando el ícono. En la mayoría, da acceso a 5 secciones — Rutinas (pantalla Home actual), Ejercicios (catálogo completo, sin depender de estar dentro de una rutina), Resumen/Reporte (dashboard con métricas agregadas entre ejercicios y rutinas: entrenamientos completados, volumen total, racha, etc. — distinto del Historial por ejercicio, que se mantiene), Herramientas (por ahora, Import/Export de datos; sección pensada para crecer con más utilidades) y Configuraciones (ver siguiente punto). En "Ejercicio activo" cambia de contenido: en vez de las 5 secciones, muestra la lista de ejercicios del día de rutina en curso (navegable — tocar uno salta directo a ese ejercicio sin perder el progreso ya registrado en la sesión), con un ítem final "Terminar rutina?" que pide confirmación ("¿Está seguro?") antes de cerrar la sesión.
- [ ] **Configuraciones**: tema claro/oscuro, tamaño/densidad de la interfaz, y sistema de unidades (métrico/imperial).
- [ ] **Rutinas**: agrupar ejercicios en una rutina (ej. "Push Day"), con orden, series objetivo por tipo de serie y descanso. Debe existir una rutina por defecto (ver sección "Rutina por defecto") y el usuario debe poder crear rutinas adicionales. El usuario puede editar cualquier rutina (agregar/mover/eliminar ejercicios dentro de un día, agregar/eliminar/renombrar días) y eliminar rutinas completas — excepto la rutina por defecto, que no se puede eliminar (sí editar su contenido).
- [ ] **Historial**: ver progreso de un ejercicio a lo largo del tiempo (peso máximo, volumen, últimas sesiones); separar volumen/progreso por tipo de serie, ya que solo las series efectivas deberían contar como volumen real.
- [ ] **Base de ejercicios por defecto**: catálogo precargado con los ejercicios más comunes (press banca, sentadilla, peso muerto, dominadas, remo, curl, etc.), incluyendo un **ejercicio base de calentamiento/cardio** con "Caminar en cinta" como primera alternativa; el usuario puede agregar más alternativas a este grupo.
- [ ] **Ejercicios alternativos**: cada ejercicio tiene una lista de sustitutos equivalentes (mismo grupo muscular/patrón de movimiento), para cambiar sobre la marcha sin perder el hilo de la rutina.
  - Ejemplo: Press banca con barra ↔ Press banca con mancuerna ↔ Press en máquina ↔ Flexiones lastradas.
  - Debe ser fácil hacer el cambio *durante* el entrenamiento, no solo al planificar.
- [ ] **Notas por ejercicio**: cada ejercicio activo permite agregar/ver comentarios o notas técnicas (ej. "ajustar asiento a nivel de barbilla"), accesibles sin salir de la pantalla de entrenamiento.
- [ ] **Galería del ejercicio**: la pantalla de "ejercicio activo" tiene 2 tabs — (a) la pantalla de entrenamiento ya descrita arriba, y (b) una galería personalizable por ejercicio (fotos/videos de referencia, ej. técnica o ajuste de máquina), que empieza vacía y el usuario va llenando con el tiempo.
- [ ] **Import/export de datos**: exportar/importar el registro completo (ejercicios, rutinas, historial) a un archivo local (JSON/CSV). El backup remoto es opcional y solo se activa si el usuario lo decide explícitamente (ligado a la pregunta abierta de cuenta de usuario).

## 4. Ideas de UX

- Pantalla de "ejercicio activo" con botón visible de "Cambiar ejercicio" que muestra alternativas sugeridas.
- Al registrar una serie, sugerir automáticamente el peso/reps de la última vez que se hizo ese ejercicio (o su alternativa); para series de calentamiento/aproximación, mostrar el peso sugerido calculado desde el % configurado sobre el peso efectivo.
- Catálogo de ejercicios con tags (grupo muscular, equipo necesario, dificultad, tags libres como "straps") para filtrar sugerencias.
- Modo "rápido": mínimos toques para registrar una serie (peso, reps, listo).
- Acceso a notas del ejercicio sin perder el contexto del entrenamiento: botón flotante o menú lateral desplegable con gesto (swipe).

## 5. Ideas futuras (post-MVP)

- Gráficas de progreso (peso máximo por ejercicio, volumen semanal), posiblemente desglosado por grupo muscular usando el % de activación.
- Plantillas de rutinas predefinidas (push/pull/legs, full body, etc.).
- Backup remoto opcional (proveedor a definir: Drive, servidor propio, etc.).
- Sugerencias de progresión automática (ej. "subiste 3 veces seguidas a 5 reps, probá +2.5kg").
- Modo offline-first (uso en el gimnasio sin buena señal).

## 6. Rutina por defecto

- El usuario ya tiene una rutina semanal en uso (ver `rutina_semanal_optimizada.md`) que debería precargarse como rutina por defecto de la app.
- **Nota**: el usuario la llama tentativamente "PPL", pero el esquema real tiene 5 días de entrenamiento (Push, Pull, Legs, Full Upper y Brazos+Pierna+Pecho), por lo que no es un PPL puro (que normalmente es de 3 o 6 días). Vale la pena renombrarla a algo más descriptivo (ej. "PPL + Upper/Accesorios") o dejar que el usuario la nombre libremente al crearla en la app.
- El usuario debe poder crear rutinas adicionales propias además de esta.
- La rutina por defecto no se puede eliminar, aunque sí se puede editar su contenido (días,
  ejercicios, esquema de series).

## 7. Preguntas abiertas

- ¿Plataforma? (web app, móvil nativa, PWA)
- ¿Cuenta de usuario / login, o todo local en el dispositivo?
- ¿Cómo se arma la base de datos de ejercicios comunes y sus alternativas? (curada a mano vs. fuente externa)
- ¿Nombre final?
- ¿De dónde sale el % de activación muscular por ejercicio? (estimación propia/curada a mano vs. alguna fuente externa)
- Si hay backup remoto opcional, ¿qué proveedor/mecanismo se usaría? (Google Drive, servidor propio, otro)
