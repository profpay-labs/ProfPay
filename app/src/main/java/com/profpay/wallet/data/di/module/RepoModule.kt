package com.profpay.wallet.data.di.module

import com.profpay.wallet.data.repository.WalletAddedRepo
import com.profpay.wallet.data.repository.WalletAddedRepoImpl
import com.profpay.wallet.data.repository.flow.AddressAndMnemonicRepo
import com.profpay.wallet.data.repository.flow.AddressAndMnemonicRepoImpl
import com.profpay.wallet.data.repository.flow.AppAccessRepo
import com.profpay.wallet.data.repository.flow.AppAccessRepoImpl
import com.profpay.wallet.data.repository.flow.BlockingAppRepo
import com.profpay.wallet.data.repository.flow.BlockingAppRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds
    @Singleton
    abstract fun bindAddressAndMnemonicRepo(addressAndMnemonicRepoImpl: AddressAndMnemonicRepoImpl): AddressAndMnemonicRepo

    @Binds
    @Singleton
    abstract fun bindBlockingAppRepo(blockingAppRepoImpl: BlockingAppRepoImpl): BlockingAppRepo

    @Binds
    @Singleton
    abstract fun bindWalletAddedRepo(walletAddedRepoImpl: WalletAddedRepoImpl): WalletAddedRepo

    @Binds
    @Singleton
    abstract fun bindAppAccessRepo(appAccessRepoImpl: AppAccessRepoImpl): AppAccessRepo
}
