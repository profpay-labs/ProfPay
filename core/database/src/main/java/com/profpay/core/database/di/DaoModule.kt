package com.profpay.core.database.di

import com.profpay.core.database.AppDatabase
import com.profpay.core.database.dao.*
import com.profpay.core.database.dao.wallet.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    fun provideProfileDao(db: AppDatabase): ProfileDao = db.getProfileDao()

    @Provides
    fun provideSettingsDao(db: AppDatabase): SettingsDao = db.getSettingsDao()

    @Provides
    fun provideTransactionsDao(db: AppDatabase): TransactionsDao = db.getTransactionsDao()

    @Provides
    fun provideAddressDao(db: AppDatabase): AddressDao = db.getAddressDao()

    @Provides
    fun provideTokenDao(db: AppDatabase): TokenDao = db.getTokenDao()

    @Provides
    fun provideWalletProfileDao(db: AppDatabase): WalletProfileDao = db.getWalletProfileDao()

    @Provides
    fun provideCentralAddressDao(db: AppDatabase): CentralAddressDao = db.getCentralAddressDao()

    @Provides
    fun provideSmartContractDao(db: AppDatabase): SmartContractDao = db.getSmartContractDao()

    @Provides
    fun provideExchangeRatesDao(db: AppDatabase): ExchangeRatesDao = db.getExchangeRatesDao()

    @Provides
    fun provideTradingInsightsDao(db: AppDatabase): TradingInsightsDao = db.getTradingInsightsDao()

    @Provides
    fun providePendingTransactionDao(db: AppDatabase): PendingTransactionDao = db.getPendingTransactionDao()

    @Provides
    fun providePendingAmlTransactionDao(db: AppDatabase): PendingAmlTransactionDao = db.getPendingAmlTransactionDao()
}
