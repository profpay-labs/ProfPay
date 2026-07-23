package com.profpay.data.transfer.di

import com.profpay.data.transfer.repository.local.PendingTransactionLocalRepositoryImpl
import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PendingTransactionLocalModule {

    @Binds
    @Singleton
    abstract fun bindPendingTransactionLocalRepository(
        impl: PendingTransactionLocalRepositoryImpl,
    ): PendingTransactionLocalRepository
}
