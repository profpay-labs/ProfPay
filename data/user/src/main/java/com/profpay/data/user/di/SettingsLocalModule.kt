package com.profpay.data.user.di

import com.profpay.data.user.repository.local.SettingsLocalRepositoryImpl
import com.profpay.domain.user.repository.local.SettingsLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsLocalModule {

    @Binds
    @Singleton
    abstract fun settingsLocalRepository(
        impl: SettingsLocalRepositoryImpl,
    ): SettingsLocalRepository
}
