package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.SubstituteRepository
import com.example.fitswap.domain.model.Exercise
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Grupos de ejercicios equivalentes (mismo grupo muscular/patrón de movimiento), curados a mano
 * sobre el catálogo existente ([ExerciseCatalog]) — ver la pregunta abierta de producto sobre el
 * origen del catálogo de sustitutos en `docs/DESIGN_DOC.md#preguntas-abiertas`. Ejercicios de
 * aislamiento/accesorio sin un sustituto claro en el catálogo actual (ej. elevaciones laterales)
 * quedan fuera de todo grupo a propósito: [substitutesFor] devuelve una lista vacía para ellos.
 */
@Singleton
class FakeSubstituteRepository @Inject constructor() : SubstituteRepository {

    private val groups: List<List<Exercise>> = substituteGroups()

    override suspend fun substitutesFor(exerciseId: String): List<Exercise> {
        val group = groups.firstOrNull { group -> group.any { it.id == exerciseId } } ?: return emptyList()
        return group.filterNot { it.id == exerciseId }
    }
}

/**
 * Grupos de ejercicios equivalentes — top-level para que tanto [FakeSubstituteRepository] como
 * `DatabaseSeeder` (M11) usen la misma data curada, en vez de mantener dos copias.
 */
internal fun substituteGroups(): List<List<Exercise>> = listOf(
    listOf(ExerciseCatalog.pressDePecho, ExerciseCatalog.pressInclinado, ExerciseCatalog.pechoPressAperturas),
    listOf(ExerciseCatalog.remoConBarraOMancuerna, ExerciseCatalog.remoChestSupported, ExerciseCatalog.remoEnMaquinaOMancuerna),
    listOf(ExerciseCatalog.jalonAlPechoDominadas, ExerciseCatalog.jalonAlPecho),
    listOf(ExerciseCatalog.sentadillaHackSquatPrensa, ExerciseCatalog.prensaSentadillaLigera),
    listOf(ExerciseCatalog.curlBicepsBarra, ExerciseCatalog.curlBicepsAlternado, ExerciseCatalog.curlMartillo),
    listOf(
        ExerciseCatalog.extensionTricepsCuerda,
        ExerciseCatalog.tricepsEnCuerda,
        ExerciseCatalog.extensionTricepsOverhead,
        ExerciseCatalog.fondosParaTriceps,
    ),
    listOf(ExerciseCatalog.pressMilitarMaquina, ExerciseCatalog.pressDeHombroEnMaquina),
)
