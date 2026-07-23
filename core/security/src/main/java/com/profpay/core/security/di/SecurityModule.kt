package com.profpay.core.security.di

import com.profpay.core.security.CryptoManager
import com.profpay.core.security.KeystoreCryptoManager
import com.profpay.core.security.pin.PinManagerImpl
import com.profpay.domain.security.repository.PinManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindPinManager(impl: PinManagerImpl): PinManager

    @Binds
    @Singleton
    abstract fun bindCryptoManager(
        impl: KeystoreCryptoManager
    ): CryptoManager
}
