package com.profpay.data.wallet.di

import com.profpay.data.wallet.repository.local.CentralAddressLocalRepositoryImpl
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CentralAddressLocalModule {

    @Binds
    @Singleton
    abstract fun bindCentralAddressLocalRepository(
        impl: CentralAddressLocalRepositoryImpl,
    ): CentralAddressLocalRepository
}
