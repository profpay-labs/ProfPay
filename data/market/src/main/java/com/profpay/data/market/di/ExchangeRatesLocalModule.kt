package com.profpay.data.market.di

import com.profpay.data.market.repository.local.ExchangeRatesLocalRepositoryImpl
import com.profpay.domain.market.repository.ExchangeRatesLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExchangeRatesLocalModule {

    @Binds
    @Singleton
    abstract fun bindExchangeRatesLocalRepository(
        impl: ExchangeRatesLocalRepositoryImpl,
    ): ExchangeRatesLocalRepository
}
