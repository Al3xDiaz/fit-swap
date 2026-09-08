package com.example.fitswap.data.local

import com.example.fitswap.data.local.entity.RoutineDayEntity
import com.example.fitswap.data.local.entity.RoutineEntity
import com.example.fitswap.data.local.entity.RoutineExerciseEntity
import com.example.fitswap.data.local.entity.SubstituteLinkEntity
import com.example.fitswap.data.local.entity.toEntities
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.buildHistorySeed
import com.example.fitswap.data.repository.fake.seedMeasurements
import com.example.fitswap.data.repository.fake.seedRoutines
import com.example.fitswap.data.repository.fake.substituteGroups
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Puebla la base Room al primer arranque (cuando las tablas todavía están vacías): mismo contenido
 * que `ExerciseCatalog`/`RoutineSeedData`/`FakeSubstituteRepository`/`FakeHistoryRepository`/
 * `FakeBodyMeasurementRepository` usaban en memoria antes de M11/M14 — se reusa esa misma data
 * (`substituteGroups()`/`buildHistorySeed()`/`seedMeasurements()` pasaron de `private` a `internal`
 * en sus archivos originales solo para esto) para que la experiencia de usuario no cambie al pasar
 * a persistencia real. El perfil corporal (M13/M14) queda deliberadamente sin seedear — el fake
 * tampoco lo pre-cargaba, a propósito, para ejercitar la rama "faltan datos".
 */
@Singleton
class DatabaseSeeder @Inject constructor(private val database: FitSwapDatabase) {

    suspend fun seedIfNeeded() {
        if (database.exerciseDao().count() > 0) {
            syncCatalog()
            return
        }

        database.exerciseDao().insertAll(ExerciseCatalog.allExercises.map { it.toEntity() })

        val links = substituteGroups().flatMapIndexed { index, group ->
            val groupId = "group-$index"
            group.map { exercise -> SubstituteLinkEntity(exerciseId = exercise.id, groupId = groupId) }
        }
        database.substituteDao().insertAll(links)

        val routineEntities = mutableListOf<RoutineEntity>()
        val dayEntities = mutableListOf<RoutineDayEntity>()
        val routineExerciseEntities = mutableListOf<RoutineExerciseEntity>()
        seedRoutines().forEach { routine ->
            val (routineEntity, days, routineExercises) = routine.toEntities()
            routineEntities += routineEntity
            dayEntities += days
            routineExerciseEntities += routineExercises
        }
        database.routineDao().insertRoutines(routineEntities)
        database.routineDao().insertDays(dayEntities)
        database.routineDao().insertRoutineExercises(routineExerciseEntities)

        database.historyDao().insertAll(buildHistorySeed().values.flatten().map { it.toEntity() })

        database.bodyMeasurementDao().insertAll(seedMeasurements().map { it.toEntity() })
    }

    /**
     * Corre en cada arranque para instalaciones ya sembradas: inserta solo los ejercicios de
     * [ExerciseCatalog.allExercises] cuyo id todavía no está en la tabla `exercises`, sin tocar los
     * existentes (nunca pisa ediciones del usuario). Necesario porque `seedIfNeeded` solo siembra
     * una vez — sin esto, un ejercicio agregado al catálogo después del lanzamiento nunca llegaría
     * a una instalación existente.
     */
    private suspend fun syncCatalog() {
        val existingIds = database.exerciseDao().allIds().toSet()
        val newExercises = ExerciseCatalog.allExercises.filterNot { it.id in existingIds }
        if (newExercises.isNotEmpty()) {
            database.exerciseDao().insertAll(newExercises.map { it.toEntity() })
        }
    }
}
