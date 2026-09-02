package com.example.fitswap.data.di

import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.repository.fake.FakeSetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindRoutineRepository(impl: FakeRoutineRepository): RoutineRepository

    @Binds
    abstract fun bindSetRepository(impl: FakeSetRepository): SetRepository

    @Binds
    abstract fun bindHistoryRepository(impl: FakeHistoryRepository): HistoryRepository
}
