package com.profpay.data.transfer.di

import com.profpay.core.network.client.RetrofitFactory
import com.profpay.data.transfer.api.TransferApi
import com.profpay.data.transfer.repository.TransferRepositoryImpl
import com.profpay.data.transfer.service.BalanceCheckerImpl
import com.profpay.domain.transfer.repository.BalanceChecker
import com.profpay.domain.transfer.repository.TransferRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object TransferApiModule {

    @Provides
    @Singleton
    fun provideTransferApi(factory: RetrofitFactory): TransferApi =
        factory.createAuthenticatedApi(TransferApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TransferRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository

    @Binds
    @Singleton
    abstract fun bindBalanceChecker(impl: BalanceCheckerImpl): BalanceChecker
}
