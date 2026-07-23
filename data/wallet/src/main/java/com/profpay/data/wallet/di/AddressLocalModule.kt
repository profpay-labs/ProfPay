package com.profpay.data.wallet.di

import com.profpay.data.wallet.repository.local.AddressLocalRepositoryImpl
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AddressLocalModule {

    @Binds
    @Singleton
    abstract fun bindAddressLocalRepository(
        impl: AddressLocalRepositoryImpl
    ): AddressLocalRepository
}
