
package com.profpay.data.market.di

import com.profpay.core.network.client.RetrofitFactory
import com.profpay.data.market.api.BinanceApi
import com.profpay.data.market.api.CoinGeckoApi
import com.profpay.data.market.repository.MarketRepositoryImpl
import com.profpay.domain.market.repository.MarketRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MarketDataModule {

    @Binds
    @Singleton
    abstract fun bindMarketRepository(impl: MarketRepositoryImpl): MarketRepository

    companion object {

        private const val BINANCE_BASE_URL = "https://api.binance.com/"
        private const val COINGECKO_BASE_URL = "https://api.coingecko.com/"

        /**
         * Binance API — публичный, без авторизации.
         */
        @Provides
        @Singleton
        fun provideBinanceApi(factory: RetrofitFactory): BinanceApi =
            factory.createPublicApi(BinanceApi::class.java, BINANCE_BASE_URL)

        /**
         * CoinGecko API — публичный, без авторизации.
         */
        @Provides
        @Singleton
        fun provideCoinGeckoApi(factory: RetrofitFactory): CoinGeckoApi =
            factory.createPublicApi(CoinGeckoApi::class.java, COINGECKO_BASE_URL)
    }
}
