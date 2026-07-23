package com.profpay.data.aml.di

import com.profpay.core.network.client.RetrofitFactory
import com.profpay.data.aml.api.AmlApi
import com.profpay.data.aml.repository.AmlRepositoryImpl
import com.profpay.domain.aml.repository.AmlRepository
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
    fun provideAmlApi(factory: RetrofitFactory): AmlApi =
        factory.createAuthenticatedApi(AmlApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AmlRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAmlRepository(impl: AmlRepositoryImpl): AmlRepository
}
