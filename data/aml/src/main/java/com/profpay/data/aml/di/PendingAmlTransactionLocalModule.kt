package com.profpay.data.aml.di

import com.profpay.data.aml.repository.local.PendingAmlTransactionLocalRepositoryImpl
import com.profpay.domain.aml.repository.PendingAmlTransactionLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PendingAmlTransactionLocalModule {

    @Binds
    @Singleton
    abstract fun bindPendingAmlTransactionLocalRepository(
        impl: PendingAmlTransactionLocalRepositoryImpl,
    ): PendingAmlTransactionLocalRepository
}
