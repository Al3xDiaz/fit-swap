package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.LoggedSet
import kotlinx.coroutines.flow.Flow

interface SetRepository {
    fun observeLoggedSets(routineExerciseId: String): Flow<List<LoggedSet>>
    suspend fun logSet(loggedSet: LoggedSet)

    /** Borra series ya persistidas por id — usado para descartar el progreso de la sesión en
     * curso cuando el usuario sale del entrenamiento sin completar el ejercicio. */
    suspend fun deleteSets(ids: List<String>)
}
