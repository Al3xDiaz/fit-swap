package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Exercise

interface SubstituteRepository {
    suspend fun substitutesFor(exerciseId: String): List<Exercise>
}
