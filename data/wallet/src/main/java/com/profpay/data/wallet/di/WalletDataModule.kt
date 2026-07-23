package com.profpay.data.wallet.di

import com.profpay.core.network.auth.WalletAuthProvider
import com.profpay.core.network.client.RetrofitFactory
import com.profpay.data.wallet.api.PublicWalletApi
import com.profpay.data.wallet.api.WalletApi
import com.profpay.data.wallet.local.ActiveWalletManagerImpl
import com.profpay.data.wallet.repository.ReissueCentralAddressRepositoryImpl
import com.profpay.data.wallet.repository.WalletRepositoryImpl
import com.profpay.data.wallet.security.PrivateKeyProviderImpl
import com.profpay.data.wallet.security.WalletAuthProviderImpl
import com.profpay.domain.security.PrivateKeyProvider
import com.profpay.domain.wallet.ActiveWalletManager
import com.profpay.domain.wallet.repository.ReissueCentralAddressRepository
import com.profpay.domain.wallet.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletDataModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindActiveWalletManager(impl: ActiveWalletManagerImpl): ActiveWalletManager

    @Binds
    @Singleton
    abstract fun bindPrivateKeyProvider(impl: PrivateKeyProviderImpl): PrivateKeyProvider

    @Binds
    @Singleton
    abstract fun bindWalletAuthProvider(impl: WalletAuthProviderImpl): WalletAuthProvider

    @Binds
    @Singleton
    abstract fun bindReissueCentralAddressRepository(impl: ReissueCentralAddressRepositoryImpl): ReissueCentralAddressRepository

    companion object {

        /**
         * Публичный API — без авторизации кошельком.
         * Используется для: создания кошелька, установки центрального адреса.
         */
        @Provides
        @Singleton
        fun providePublicWalletApi(factory: RetrofitFactory): PublicWalletApi =
            factory.createPublicApi(PublicWalletApi::class.java)

        /**
         * Защищённый API — с авторизацией кошельком.
         * Используется для: обновления derived index, получения данных кошелька.
         */
        @Provides
        @Singleton
        fun provideWalletApi(factory: RetrofitFactory): WalletApi =
            factory.createAuthenticatedApi(WalletApi::class.java)
    }
}
