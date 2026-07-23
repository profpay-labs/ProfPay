package com.profpay.data.market.di

import com.profpay.data.market.repository.local.TradingInsightsLocalRepositoryImpl
import com.profpay.domain.market.repository.TradingInsightsLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TradingInsightsLocalModule {

    @Binds
    @Singleton
    abstract fun bindTradingInsightsLocalRepository(
        impl: TradingInsightsLocalRepositoryImpl,
    ): TradingInsightsLocalRepository
}
