package com.profpay.data.contract.di

import com.profpay.core.network.client.RetrofitFactory
import com.profpay.data.contract.api.ContractApi
import com.profpay.data.contract.repository.ContractRepositoryImpl
import com.profpay.domain.contract.repository.ContractRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ContractApiModule {

    @Provides
    @Singleton
    fun provideContractApi(factory: RetrofitFactory): ContractApi =
        factory.createAuthenticatedApi(ContractApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ContractRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContractRepository(impl: ContractRepositoryImpl): ContractRepository
}
