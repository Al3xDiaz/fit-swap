package com.example.fitswap.domain.model

import kotlinx.serialization.Serializable

/**
 * Formato de exportación/importación (Herramientas, M9). Incluye solo datos durables: rutinas,
 * historial, notas y configuración. Deliberadamente **no** incluye `SetRepository` (series de un
 * entrenamiento en curso) ni las sustituciones de `WorkoutSessionRepository` — son estado de sesión
 * efímero, no datos que un usuario esperaría "hacer backup" (mismo criterio que usará el modelo de
 * datos de Room en M11: esas dos no tienen entidad persistente en `docs/DESIGN_DOC.md`).
 */
@Serializable
data class ExportedData(
    val routines: List<Routine>,
    val history: List<HistoryPoint>,
    val notes: List<Note>,
    val settings: AppSettings,
)
