package com.profpay.data.wallet.di

import com.profpay.data.wallet.repository.local.TokenLocalRepositoryImpl
import com.profpay.domain.wallet.repository.local.TokenLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TokenLocalModule {

    @Binds
    @Singleton
    abstract fun bindTokenLocalRepository(
        impl: TokenLocalRepositoryImpl,
    ): TokenLocalRepository
}
