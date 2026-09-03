package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.BodyProfile
import kotlinx.coroutines.flow.Flow

interface BodyProfileRepository {
    fun observeProfile(): Flow<BodyProfile>
    suspend fun updateProfile(profile: BodyProfile)
}
