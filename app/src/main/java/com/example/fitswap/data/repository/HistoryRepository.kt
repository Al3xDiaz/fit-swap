package com.example.fitswap.data.repository

interface HistoryRepository {
    suspend fun lastWeightKg(exerciseId: String): Double?
}
