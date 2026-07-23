package com.profpay.data.user.di

import com.profpay.data.user.repository.local.ProfileRepositoryImpl
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileLocalModule {

    @Binds
    @Singleton
    abstract fun bindProfileLocalRepository(
        impl: ProfileRepositoryImpl,
    ): ProfileLocalRepository
}
