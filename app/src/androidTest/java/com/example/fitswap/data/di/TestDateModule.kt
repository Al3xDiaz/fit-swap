package com.example.fitswap.data.di

import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.data.time.FixedCurrentDateProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DateModule::class])
abstract class TestDateModule {

    @Binds
    abstract fun bindCurrentDateProvider(impl: FixedCurrentDateProvider): CurrentDateProvider
}
