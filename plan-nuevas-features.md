# Plan: bug de ejercicio "pegado" completado + modal guardar/descartar + stepper de medidas

## Contexto

El usuario probó la app tras el pase anterior (rutinas/historial/medidas) y reportó un bug real:
un ejercicio que se completa una vez queda marcado "completado" para siempre, incluso si esa
sesión de entrenamiento nunca se guarda — porque `logged_sets` (las series registradas) **no
tiene fecha**: se acumulan sin límite por `routineExerciseId`, así que alcanzar el tamaño del plan
una vez basta para que el ejercicio se vea completo en cualquier día futuro. Pidió además una
forma explícita de "descartar datos" de un ejercicio, y que el modal de "terminar rutina" (hoy
solo confirma "¿Está seguro?") pregunte directamente si guardar o descartar lo registrado.

Aparte, dos features de UX para cargar medidas corporales: un selector previo (stepper) de qué
medidas se van a ingresar antes de mostrar el formulario, y una forma simple de editar una medición
ya guardada (hoy solo se puede agregar o eliminar).

**Decisiones de alcance acordadas con el usuario** (no re-litigar):
- El fix del bug es el **estructural completo**: agregar fecha a `logged_sets` y filtrar por el día
  actual (no solo un botón parche) — *y además* terminar la rutina sin guardar debe limpiar
  correctamente ese estado con el nuevo modelo con fecha.
- "Descartar" en el modal de terminar rutina borra **todo lo registrado en la sesión de hoy**
  (todos los ejercicios, no solo el actual).
- El modal unificado Guardar/Descartar/Cancelar reemplaza **todos** los puntos de salida — drawer
  ("Terminar rutina"), back del sistema, y el botón "Terminar y guardar" del último ejercicio
  (agregado en el pase anterior) se elimina, queda redundante.
- El selector de medidas y la edición: "algo sencillo está bien" — sin sobre-diseñar.

Este archivo cubre todo en 5 commits independientes (convención del usuario: un commit por
implementación). Se pensó para que **otra sesión** lo retome y ejecute tal cual, sin research
adicional — cada parte cita archivos, firmas y líneas exactas relevadas de la sesión actual.

---

# PARTE 1 — Fix estructural: fecha en `logged_sets`, filtrado por el día actual

## Causa raíz (confirmada leyendo el código)

`LoggedSetEntity` (`data/local/entity/LoggedSetEntity.kt:22-28`) no tiene columna `date`. `SetDao`
(`data/local/dao/SetDao.kt:12-13`) hace `SELECT * FROM logged_sets WHERE routineExerciseId = :id`
sin filtro de fecha — `observeLoggedSets()` devuelve **todo lo histórico acumulado** para ese slot.

La cadena exacta (`ActiveExerciseViewModel.kt`):
1. `registerSet()` (líneas 436-472) persiste cada serie de inmediato en Room.
2. `completeExercise()` (líneas 493-503) vacía `sessionLoggedSetIds`/`sessionHistoryPointIds` — el
   VM "olvida" qué borrar de ese ejercicio a partir de ahí.
3. Al reabrir ese mismo `RoutineExercise` (cualquier día futuro), líneas 202-204:
   ```kotlin
   val existingLogged = setRepository.observeLoggedSets(exercise.id).first()
   planIndex = existingLogged.size.coerceAtMost(plan.size)
   ```
   Como `existingLogged` incluye TODO lo histórico, `planIndex == plan.size` de entrada →
   `isComplete = true` en `refreshUiState()` (línea 577) → `registerSet()` hace `return` porque
   `plan.getOrNull(planIndex)` es null (línea 438).
4. Bug adyacente confirmado: en ese estado (`isComplete=true`, sin progreso pendiente de esta
   sesión) `completionCountdownSeconds` queda `null` (líneas 581-591), y el botón
   `completeExerciseButton` vive **dentro** de `completionCountdownSeconds?.let { ... }`
   (`ActiveExerciseScreen.kt:355-369`) — pantalla sin ningún botón, solo Anterior/Siguiente/back.
5. Mismo mecanismo en `SessionMenuViewModel.kt:55-59` — marca `DONE` con el mismo conteo histórico.

`WorkoutSessionRepository.clearAll()` (única implementación real: `fake/FakeWorkoutSessionRepository.kt`)
**no toca** `logged_sets` ni `history_points`, solo limpia sustituciones — confirmado en
`data/repository/WorkoutSessionRepository.kt:15-16`.

## Fix

1. **`domain/model/LoggedSet.kt`**: agregar `date: LocalDate` (sin default — obliga a revisar cada
   construcción, mismo criterio de rigor que otros campos "nunca inventar/omitir").
2. **`data/local/entity/LoggedSetEntity.kt`**: agregar columna `date: LocalDate` + actualizar
   `toDomain()`/`toEntity()`.
3. **`data/local/dao/SetDao.kt`**:
   - `observeLoggedSets(routineExerciseId, date)` — agrega el parámetro `date` y filtra
     `WHERE routineExerciseId = :routineExerciseId AND date = :date`.
   - Nueva `deleteLoggedSetsForDate(routineExerciseId, date)` — `DELETE ... WHERE routineExerciseId = :routineExerciseId AND date = :date` (para Parte 2).
   - Nueva `deleteAllLoggedSetsForDate(date)` — `DELETE ... WHERE date = :date`, sin filtrar por
     ejercicio (para Parte 3).
4. **`data/repository/SetRepository.kt`** (+ `room/RoomSetRepository.kt`, `fake/FakeSetRepository.kt`):
   reflejar las 3 firmas nuevas/cambiadas.
5. **`data/local/FitSwapDatabase.kt`**: bump `version = 4` → `5` con comentario al estilo de los
   anteriores (`fallbackToDestructiveMigration` ya cubre el bump, sin usuarios reales todavía).
6. **`ActiveExerciseViewModel.kt`**:
   - Línea 202: `setRepository.observeLoggedSets(exercise.id, currentDateProvider.today())`.
   - `registerSet()` (línea 443-449): agregar `date = currentDateProvider.today()` al `LoggedSet`.
   - Actualizar el KDoc de `sessionLoggedSetIds`/`sessionHistoryPointIds` (líneas 151-157): ya no
     son "qué borrar al salir" (eso ahora es por fecha, Parte 3) — pasan a ser solo "hay algo
     pendiente de auto-completar en esta pantalla" (uso real que conservan en `completeExercise()`
     y `refreshUiState()`'s `hasPendingProgress`).
7. **`SessionMenuViewModel.kt`** línea 55: `setRepository.observeLoggedSets(routineExercise.id, currentDateProvider.today()).first().size` — necesita inyectar `CurrentDateProvider` (nueva dependencia en el constructor).

**Tests** (blast radius esperado, mismo patrón que la Parte 4 del pase anterior):
- Grep `LoggedSet(` en `app/src/test` y `app/src/androidTest` — cada construcción necesita
  `date = ...` agregado (ej. `SessionMenuViewModelTest.kt`, `ActiveExerciseViewModelTest.kt`).
- `RoomSetRepositoryTest` (crear si no existe) o extender un test de DAO existente: series de hoy
  vs. de otro día no se mezclan; `deleteLoggedSetsForDate`/`deleteAllLoggedSetsForDate` borran lo
  esperado sin afectar otras fechas/ejercicios.
- Test de regresión explícito: completar un ejercicio (registrar todas sus series con fecha de
  ayer/otro día vía el fake), crear un ViewModel nuevo apuntando al mismo `routineExerciseId` con
  `currentDateProvider` en "hoy" → el ejercicio **no** debe aparecer completo, debe arrancar en
  `WARMUP` como si nunca se hubiera tocado hoy.

---

# PARTE 2 — Botón "Descartar datos" del ejercicio actual

Complementa la Parte 1: aunque ya no queda "pegado" entre días distintos, el usuario puede querer
rehacer un ejercicio **dentro del mismo día** (se equivocó de peso, quiere repetirlo, etc.) — hoy
no hay forma de resetear un slot ya completado sin borrar toda la rutina.

## Fix

1. **`ActiveExerciseViewModel.kt`**: nueva función `discardExerciseData()`:
   ```kotlin
   fun discardExerciseData() {
       val exercise = routineExercise ?: return
       val displayExercise = substitutedExercise ?: exercise.exercise
       completionCountdownJob?.cancel()
       completionCountdownJob = null
       viewModelScope.launch {
           setRepository.deleteLoggedSetsForDate(exercise.id, currentDateProvider.today())
           historyRepository.deleteHistoryForDate(displayExercise.id, currentDateProvider.today())
           sessionLoggedSetIds.clear()
           sessionHistoryPointIds.clear()
           planIndex = 0
           loggedSetSeq = 0
           manualStageWeightOverrideKg = null
           refreshUiState()
       }
   }
   ```
   Reusa `HistoryRepository.deleteHistoryForDate(exerciseId, date)` (**ya existe**,
   `HistoryRepository.kt:34`, agregado en el pase anterior para la edición de historial).
2. **`ActiveExerciseUiState`**: nuevo campo `hasLoggedDataToday: Boolean`, calculado en
   `refreshUiState()` como `planIndex > 0` (cubre completo y a medias). Solo tiene sentido para
   `ExerciseType.STRENGTH` — cardio nunca usa `planIndex`/`isComplete` (rama separada en el
   `collect` del `init`, líneas 212-231), así que el botón no debe mostrarse ahí.
3. **`ActiveExerciseScreen.kt`** (`TrainingTab`): agregar un `OutlinedButton` "Descartar datos de
   hoy" (testTag `discardExerciseDataButton`) visible cuando `uiState.hasLoggedDataToday`, ubicado
   antes del bloque `if (uiState.isComplete)` para que se vea tanto completo como a medias. Con
   `ConfirmDialog` (reusar `ui/common/ConfirmDialog.kt` tal cual, 2 opciones alcanza acá): título
   "¿Descartar datos?", mensaje "Se borrará lo registrado hoy para este ejercicio. No se puede
   deshacer.".

**Tests**: unit en `ActiveExerciseViewModelTest` — registrar series (algunas), completar o no,
llamar `discardExerciseData()`, verificar que `setRepository`/`historyRepository` quedan sin datos
de hoy para ese ejercicio, `planIndex`/`loggedSetSeq` vuelven a 0, y que otro ejercicio del día no
se ve afectado. Instrumentado: completar un ejercicio, tocar "Descartar datos de hoy", confirmar,
verificar que vuelve a `currentStageLabel == "Calentamiento"`.

---

# PARTE 3 — Modal unificado "Guardar y terminar" / "Descartar" / "Cancelar"

## Diseño actual (relevado, 3 puntos de salida distintos y confusos)

- **Drawer** (`SessionDrawerContent.kt:71-98`): item "Terminar rutina" → `ConfirmDialog` genérico
  ("¿Está seguro?") → `SessionMenuViewModel.endRoutine()` (línea 73-75, **solo** limpia
  sustituciones, conserva todo — nombre engañoso, homónimo del método de `ActiveExerciseViewModel`
  que hace algo distinto).
- **Back del sistema** (`ActiveExerciseScreen.kt:106,213-224`): `showExitConfirm` →
  `ActiveExerciseViewModel.endRoutine()` (líneas 525-533, borra el progreso del ejercicio **actual**
  únicamente, vía los ids trackeados en sesión).
- **Botón "Terminar y guardar"** del último ejercicio (`ActiveExerciseScreen.kt:226-262`,
  `finishRoutineButton` + `showFinishConfirm`) → `finishRoutineKeepingProgress()` (líneas 540-546,
  conserva todo). **Se elimina en esta parte** — el modal unificado lo hace redundante.
- `onExitDiscarding`/`onFinishRoutine` en `RoutineNavigation.kt:109-120` tienen **cuerpos
  idénticos** (navegan a Rutinas) — se colapsan en un solo callback.
- `ConfirmDialog` (`ui/common/ConfirmDialog.kt:10-26`) solo soporta 2 botones (`confirmButton`/
  `dismissButton` de `AlertDialog`) — no alcanza para Guardar/Descartar/Cancelar.

## Fix

1. **Nuevo `data/session/RoutineSessionFinisher.kt`** — clase inyectable (no repositorio, un
   caso de uso) que centraliza la lógica de "terminar la rutina" para no duplicarla entre
   `SessionMenuViewModel` y `ActiveExerciseViewModel`:
   ```kotlin
   @Singleton
   class RoutineSessionFinisher @Inject constructor(
       private val setRepository: SetRepository,
       private val historyRepository: HistoryRepository,
       private val cardioSessionRepository: CardioSessionRepository,
       private val notesRepository: NotesRepository,
       private val workoutSessionRepository: WorkoutSessionRepository,
       private val currentDateProvider: CurrentDateProvider,
   ) {
       /** Conserva todo lo registrado hoy — solo limpia sustituciones de la sesión. */
       suspend fun saveAndFinish() {
           workoutSessionRepository.clearAll()
       }

       /** Borra TODO lo registrado hoy (series, historial, sesiones de cardio, notas) de
        * cualquier ejercicio de la rutina, y limpia sustituciones. */
       suspend fun discardAndFinish() {
           val today = currentDateProvider.today()
           setRepository.deleteAllLoggedSetsForDate(today)
           historyRepository.deleteAllHistoryForDate(today)
           cardioSessionRepository.deleteAllSessionsForDate(today)
           notesRepository.deleteAllNotesForDate(today)
           workoutSessionRepository.clearAll()
       }
   }
   ```
2. Nuevos métodos "borrar todo lo de una fecha" (mismo patrón `DELETE ... WHERE date = :date` que
   ya existe para `history_points`/`logged_sets`, Parte 1):
   - `HistoryDao.deleteAllForDate(date)` + `HistoryRepository.deleteAllHistoryForDate(date)` (Room
     + Fake).
   - `CardioSessionDao.deleteAllForDate(date)` + `CardioSessionRepository.deleteAllSessionsForDate(date)`
     (Room + Fake).
   - `NoteDao.deleteAllForDate(date)` + `NotesRepository.deleteAllNotesForDate(date)` (Room + Fake).
3. **Nuevo `ui/common/FinishRoutineDialog.kt`**:
   ```kotlin
   @Composable
   fun FinishRoutineDialog(onSave: () -> Unit, onDiscard: () -> Unit, onCancel: () -> Unit) {
       AlertDialog(
           onDismissRequest = onCancel,
           title = { Text("¿Terminar rutina?") },
           text = { Text("Podés guardar lo registrado hoy o descartarlo. Descartar no se puede deshacer.") },
           confirmButton = { TextButton(onClick = onSave, modifier = Modifier.testTag("saveRoutineButton")) { Text("Guardar y terminar") } },
           dismissButton = {
               Row {
                   TextButton(onClick = onDiscard, modifier = Modifier.testTag("discardRoutineButton")) { Text("Descartar") }
                   TextButton(onClick = onCancel, modifier = Modifier.testTag("cancelFinishRoutineButton")) { Text("Cancelar") }
               }
           }
       )
   }
   ```
4. **`SessionMenuViewModel.kt`**: inyectar `RoutineSessionFinisher`, reemplazar `endRoutine()` por
   `saveRoutine()`/`discardRoutine()` delegando en el finisher.
5. **`SessionDrawerContent.kt`**: reemplazar el `ConfirmDialog` de "Terminar rutina" por
   `FinishRoutineDialog`, cableando `onSave`/`onDiscard` a los dos métodos nuevos.
6. **`ActiveExerciseViewModel.kt`**: reemplazar `endRoutine()`/`finishRoutineKeepingProgress()` por
   `saveAndFinishRoutine()`/`discardAndFinishRoutine()` — ambos cancelan el countdown y limpian
   `sessionLoggedSetIds`/`sessionHistoryPointIds` localmente, y delegan en
   `routineSessionFinisher.saveAndFinish()`/`discardAndFinish()` (nueva dependencia inyectada).
7. **`ActiveExerciseScreen.kt`**:
   - `BackHandler` sigue disparando el diálogo, ahora `FinishRoutineDialog` en vez de `ConfirmDialog`.
   - Eliminar el botón `finishRoutineButton` de `NextPreviousBar` (líneas 226-262) y el parámetro
     `onFinishRoutineClick` — ya no hace falta, el modal se alcanza siempre por back o por el drawer.
   - Colapsar `onExitDiscarding`/`onFinishRoutine` en un único `onRoutineFinished: () -> Unit`.
8. **`RoutineNavigation.kt`**: `ActiveExerciseScreen(onRoutineFinished = { navigate(Routines){popUpTo...} }, ...)` — un solo callback en vez de dos idénticos.

**Tests** (blast radius grande, esperarlo — mismo patrón que Parte 4 del pase anterior):
- Unit: `SessionMenuViewModelTest`, `ActiveExerciseViewModelTest` — reescribir los tests que
  usaban `endRoutine()`/`finishRoutineKeepingProgress()` para los nuevos nombres/semántica; agregar
  un test de `discardAndFinish()` que registre datos en 2 ejercicios distintos del día y verifique
  que **ambos** quedan sin datos de hoy (a diferencia del descarte por ejercicio de la Parte 2).
- Instrumentado — actualizar todos los `onNodeWithText("Confirmar")` tras clickear
  `finishRoutineItem`/back: `SessionMenuAndNotesFlowTest.kt:70-72`, `CardioHistoryFlowTest.kt:53-54`,
  `ActiveExerciseFlowTest.kt:85-86` → ahora clickean `saveRoutineButton` o `discardRoutineButton`
  según el caso. Actualizar también el test que usaba `finishRoutineButton` (ya no existe).
- Nuevo test instrumentado: registrar una serie, tocar back, elegir "Descartar" → volver a entrar
  a esa rutina, el ejercicio debe estar vacío (no completado, no con la serie).

---

# PARTE 4 — Selector de medidas a ingresar (stepper) antes del formulario

Hoy `AddBodyMeasurementScreen` (`ui/screens/measurements/AddBodyMeasurementScreen.kt`) muestra los
12 campos (peso obligatorio + 11 circunferencias opcionales) de una sola vez. No existe ningún
patrón de wizard/stepper en la app (relevado con grep — el único `HorizontalPager` es de tabs en
`ActiveExerciseScreen.kt`, no de wizard) — este es el primero.

## Fix

1. **Nuevo `domain/model/MeasurementField.kt`**, enum compartido (elimina la lista duplicada que
   hoy vive privada en `MeasurementGuideScreen.kt:21-92`):
   ```kotlin
   enum class MeasurementField(val label: String, val tag: String) {
       NECK("Cuello", "neck"), WAIST("Cintura", "waist"), HIP("Cadera", "hip"),
       CHEST("Pecho", "chest"), ARM("Brazo", "arm"), LEG("Pierna", "leg"),
       CALF("Pantorrilla", "calf"), GLUTE("Glúteo", "glute"), FOREARM("Antebrazo", "forearm"),
       SHOULDER("Hombro", "shoulder"), WRIST("Muñeca", "wrist"),
   }
   ```
   (Opcional pero recomendado dentro de esta misma parte: hacer que `MeasurementGuideScreen`
   itere `MeasurementField.entries` para el id/título, guardando la instrucción de técnica en un
   `Map<MeasurementField, String>` aparte — evita mantener dos listas de las mismas 11 medidas.)
2. **`AddBodyMeasurementViewModel.kt`**: `AddBodyMeasurementUiState` suma `step: Int = 0` (0 =
   selección de campos, 1 = formulario) y `selectedFields: Set<MeasurementField> = emptySet()`.
   Nuevas funciones `toggleField(field: MeasurementField)`, `goToForm()` (`step = 1`),
   `goBackToFieldSelection()` (`step = 0`). `save()` **no cambia** — ya trata cada campo de texto
   vacío como ausente (`toDoubleOrNull()`), así que un campo no seleccionado simplemente nunca se
   tocó y queda `""` → `null`.
3. **`AddBodyMeasurementScreen.kt`**: `when (uiState.step) { 0 -> FieldSelectionStep(...); else ->
   MeasurementFormStep(...) }`.
   - `FieldSelectionStep`: título "¿Qué medidas vas a registrar hoy?", una fila `Checkbox` +
     `Text(field.label)` por cada `MeasurementField.entries` (testTag `fieldOption_${field.tag}`),
     botón "Siguiente" (testTag `nextStepButton`, siempre habilitado — se puede continuar sin
     elegir ninguna, solo peso) → `viewModel.goToForm()`.
   - `MeasurementFormStep`: el peso (`weightField`) siempre visible + un `OutlinedTextField` solo
     por cada campo en `uiState.selectedFields` (mismos testTags/labels que hoy: `neckField`,
     `waistField`, etc. — no cambian). Agregar `OutlinedButton` "Atrás" (testTag
     `backToFieldSelectionButton`) → `viewModel.goBackToFieldSelection()`, junto al botón "Guardar"
     existente (sin cambios en su lógica).
   - El botón de guía (`measurementGuideButton`) queda visible en ambos pasos.

**Tests**: `AddBodyMeasurementViewModelTest` — `toggleField` agrega/quita del set;
`goToForm`/`goBackToFieldSelection` cambian `step`; `save()` con un campo no seleccionado lo deja
`null` aunque el ViewModel tuviera texto viejo en ese campo de una selección anterior (verificar que
deseleccionar no deja basura). Instrumentado: reescribir
`BodyMeasurementsFlowTest.agregarUnaMedicionCompletaAparecEnElHistorialConSuImcYPorcentajeDeGrasa`
(hoy escribe directo en `neckField`/`waistField` sin pasos intermedios) para primero tildar
`fieldOption_neck`/`fieldOption_waist`, tocar `nextStepButton`, y recién ahí completar los campos.

---

# PARTE 5 — Editar una medición corporal ya guardada

Hoy `BodyMeasurementRepository` (`data/repository/BodyMeasurementRepository.kt`) solo tiene
`observeMeasurements()`/`addMeasurement()`/`deleteMeasurement()`. La infraestructura ya soporta
upsert sin migración: `BodyMeasurementDao.insert` usa `OnConflictStrategy.REPLACE`
(`data/local/dao/BodyMeasurementDao.kt:15-16`) y `BodyMeasurementEntity` tiene `@PrimaryKey val id`.

## Fix

1. **`BodyMeasurementRepository.kt`**: agregar `suspend fun updateMeasurement(entry: BodyMeasurementEntry)`.
   - Room (`RoomBodyMeasurementRepository.kt`): `bodyMeasurementDao.insert(entry.toEntity())` —
     mismo upsert que `addMeasurement`.
   - Fake (`FakeBodyMeasurementRepository.kt`): **corregir** `addMeasurement`, que hoy hace
     `current + entry` (append ciego, duplicaría la fila si el id ya existe) — refactorizar a un
     `private fun upsert(entry)` compartido por `addMeasurement`/`updateMeasurement` que reemplaza
     por id, mismo patrón que `FakeCardioSessionRepository`/`FakeHistoryRepository` (commits del
     pase anterior).
2. **Nuevo `ui/screens/measurements/MeasurementEditScreen.kt` + `MeasurementEditViewModel.kt`** —
   plantilla directa de `ui/screens/exercises/CardioSessionEditScreen.kt`/`CardioSessionEditViewModel.kt`:
   pantalla de una sola página (sin stepper — edición simple), `measurementId` desde
   `SavedStateHandle`, precarga los 12 campos desde la medición existente (`date` se conserva, no
   es editable), `save()` llama `updateMeasurement(...)` con el mismo id y fecha original. TestTags
   prefijados `edit` (`editWeightField`, `editNeckField`, ..., `saveMeasurementEditButton`) para no
   pisar los de `AddBodyMeasurementScreen`.
3. **`BodyMeasurementsScreen.kt`** (`MeasurementRow`, líneas ~93-138): agregar un `IconButton`
   (`Icons.Default.Edit`, testTag `editMeasurementButton_${row.entry.id}`) junto al de eliminar
   existente (`deleteMeasurementButton_${id}`), que navega a `editBodyMeasurement/{id}`.
4. **`navigation/MeasurementsNavigation.kt`**: ruta privada
   `EDIT_BODY_MEASUREMENT_ROUTE = "editBodyMeasurement"` + `navArgument`, función
   `measurementEditScreen(navController)`, registrada en `AppNavHost.kt` (mismo patrón que
   `cardioSessionEditScreen`).

**Tests**: `MeasurementEditViewModelTest` (nuevo, plantilla `CardioSessionEditViewModelTest`) —
precarga correcta, guardar conserva id y fecha originales con los campos editados. Nuevo test en
`FakeBodyMeasurementRepositoryTest` (crear si no existe) — `addMeasurement` con un id repetido
reemplaza en vez de duplicar; `updateMeasurement` idem. Instrumentado: agregar al
`BodyMeasurementsFlowTest` un caso que edite una medición ya listada y verifique el cambio
reflejado en `bmiLabel_${id}`/`bodyFatLabel_${id}` si corresponde.

---

# Orden de commits

1. `fix: agregar fecha a las series registradas y filtrar por el dia actual`
2. `feat: boton para descartar el registro de hoy de un ejercicio`
3. `feat: modal de terminar rutina con opciones guardar, descartar y cancelar`
4. `feat: selector de medidas a ingresar antes del formulario`
5. `feat: editar una medicion corporal ya guardada`

Cada commit debe compilar y pasar `./gradlew test` de forma independiente. El 2 y el 3 dependen del
1 (necesitan las columnas/queries de fecha en `logged_sets`); el 5 es independiente del 4 pero
comparten el enum `MeasurementField` si se hace la limpieza opcional de la guía en la Parte 4 —
en ese caso, hacer el 4 antes del 5, o simplemente no depender de él (`MeasurementEditScreen` no
necesita el enum, puede tener sus 12 campos hardcodeados igual que hoy).

# Verificación

- `./gradlew test` — unitarios JVM (incluye los tests nuevos/actualizados de cada parte).
- `./gradlew lint`.
- `./gradlew assembleDebug`.
- `./gradlew connectedAndroidTest` — **requiere emulador/dispositivo, no disponible en este
  entorno** (mismo caveat que los dos pases anteriores): los instrumentados se escriben y
  compilan, pero no se ejecutan hasta correrlos en un device real.
- Verificación manual imprescindible:
  - Parte 1: completar un ejercicio, salir sin guardar (o terminar guardando), y reabrir ese mismo
    ejercicio al día siguiente (o cambiando la fecha del dispositivo) — no debe aparecer completo.
  - Parte 2: completar un ejercicio y tocar "Descartar datos de hoy" — debe volver a Calentamiento.
  - Parte 3: desde el drawer y desde el back, el modal debe ofrecer Guardar/Descartar/Cancelar;
    "Descartar" debe vaciar TODO lo registrado hoy en la rutina, no solo el ejercicio actual.
  - Parte 4: en "Nueva medición", el primer paso debe pedir qué medir antes de mostrar campos.
  - Parte 5: editar una medición ya guardada debe reflejarse en la lista sin duplicarla.
