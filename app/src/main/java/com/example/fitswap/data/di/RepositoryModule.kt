package com.example.fitswap.data.di

import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindRoutineRepository(impl: FakeRoutineRepository): RoutineRepository
}
