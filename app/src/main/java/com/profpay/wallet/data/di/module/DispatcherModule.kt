package com.profpay.wallet.data.di.module

import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.common.di.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): @JvmSuppressWildcards CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun provideMainDispatcher(): @JvmSuppressWildcards CoroutineDispatcher = Dispatchers.Main
}
