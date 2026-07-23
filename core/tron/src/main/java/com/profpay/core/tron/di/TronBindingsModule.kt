package com.profpay.core.tron.di

import com.profpay.core.tron.Tron
import com.profpay.core.tron.api.TronAccountsApi
import com.profpay.core.tron.api.TronAddressApi
import com.profpay.core.tron.api.TronEstimateApi
import com.profpay.core.tron.api.TronHttpApi
import com.profpay.core.tron.api.TronSmartContractApi
import com.profpay.core.tron.api.TronStakingApi
import com.profpay.core.tron.api.TronTransactionsApi
import com.profpay.core.tron.impl.AccountsImpl
import com.profpay.core.tron.impl.AddressUtilitiesImpl
import com.profpay.core.tron.impl.EstimateImpl
import com.profpay.core.tron.impl.SmartContractImpl
import com.profpay.core.tron.impl.StakingImpl
import com.profpay.core.tron.impl.TransactionsImpl
import com.profpay.core.tron.impl.TronHttpApiImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TronBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTronAddressApi(impl: AddressUtilitiesImpl): TronAddressApi

    @Binds
    @Singleton
    abstract fun bindTronAccountsApi(impl: AccountsImpl): TronAccountsApi

    @Binds
    @Singleton
    abstract fun bindTronTransactionsApi(impl: TransactionsImpl): TronTransactionsApi

    @Binds
    @Singleton
    abstract fun bindTronStakingApi(impl: StakingImpl): TronStakingApi

    @Binds
    @Singleton
    abstract fun bindTronSmartContractApi(impl: SmartContractImpl): TronSmartContractApi

    @Binds
    @Singleton
    abstract fun bindTronHttpApi(impl: TronHttpApiImpl): TronHttpApi

    @Binds
    @Singleton
    abstract fun bindTronEstimateApi(impl: EstimateImpl): TronEstimateApi
}

@Module
@InstallIn(SingletonComponent::class)
object TronProviderModule {

    @Provides
    @Singleton
    fun provideTron(
        addressApi: TronAddressApi,
        accountsApi: TronAccountsApi,
        transactionsApi: TronTransactionsApi,
        stakingApi: TronStakingApi,
        smartContractApi: TronSmartContractApi,
        httpApi: TronHttpApi,
        estimatesApi: TronEstimateApi,
    ): Tron = Tron(
        addressUtilities = addressApi,
        accounts = accountsApi,
        transactions = transactionsApi,
        staking = stakingApi,
        smartContracts = smartContractApi,
        http = httpApi,
        estimates = estimatesApi,
    )
}
