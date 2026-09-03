package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.BodyProfileDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.BodyProfileRepository
import com.example.fitswap.domain.model.BodyProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomBodyProfileRepository @Inject constructor(
    private val bodyProfileDao: BodyProfileDao,
) : BodyProfileRepository {

    override fun observeProfile(): Flow<BodyProfile> =
        bodyProfileDao.observeProfile().map { it?.toDomain() ?: BodyProfile() }

    override suspend fun updateProfile(profile: BodyProfile) {
        bodyProfileDao.upsert(profile.toEntity())
    }
}
