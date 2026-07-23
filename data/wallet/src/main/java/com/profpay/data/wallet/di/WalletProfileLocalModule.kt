package com.profpay.data.wallet.di

import com.profpay.data.wallet.repository.local.WalletProfileLocalRepositoryImpl
import com.profpay.domain.wallet.repository.local.WalletProfileLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletProfileLocalModule {

    @Binds
    @Singleton
    abstract fun bindWalletProfileLocalRepository(
        impl: WalletProfileLocalRepositoryImpl,
    ): WalletProfileLocalRepository
}
