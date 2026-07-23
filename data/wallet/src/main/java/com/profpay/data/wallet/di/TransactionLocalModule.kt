package com.profpay.data.wallet.di

import com.profpay.data.wallet.repository.local.TransactionLocalRepositoryImpl
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionLocalModule {

    @Binds
    @Singleton
    abstract fun bindTransactionLocalRepository(
        impl: TransactionLocalRepositoryImpl,
    ): TransactionLocalRepository
}
