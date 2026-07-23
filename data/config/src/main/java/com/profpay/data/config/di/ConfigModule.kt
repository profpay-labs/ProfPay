package com.profpay.data.config.di

import com.profpay.core.network.client.RetrofitFactory
import com.profpay.data.config.api.ConfigApi
import com.profpay.data.config.repository.ConfigRepositoryImpl
import com.profpay.domain.config.repository.ConfigRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AmlApiModule {

    @Provides
    @Singleton
    fun provideConfigApi(factory: RetrofitFactory): ConfigApi =
        factory.createAuthenticatedApi(ConfigApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ConfigRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConfigRepository(impl: ConfigRepositoryImpl): ConfigRepository
}
