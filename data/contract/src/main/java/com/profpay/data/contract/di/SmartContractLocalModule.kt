package com.profpay.data.contract.di

import com.profpay.data.contract.repository.local.SmartContractLocalRepositoryImpl
import com.profpay.domain.contract.repository.SmartContractLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SmartContractLocalModule {

    @Binds
    @Singleton
    abstract fun bindSmartContractLocalRepository(
        impl: SmartContractLocalRepositoryImpl,
    ): SmartContractLocalRepository
}
