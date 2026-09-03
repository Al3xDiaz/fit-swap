package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.BodyProfileRepository
import com.example.fitswap.domain.model.BodyProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class FakeBodyProfileRepository @Inject constructor() : BodyProfileRepository {

    private val profile = MutableStateFlow(BodyProfile())

    override fun observeProfile(): Flow<BodyProfile> = profile.asStateFlow()

    override suspend fun updateProfile(profile: BodyProfile) {
        this.profile.value = profile
    }
}
