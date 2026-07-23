package com.profpay.data.user.di

import com.profpay.core.network.client.RetrofitFactory
import com.profpay.data.user.api.PublicUserApi
import com.profpay.data.user.api.UserApi
import com.profpay.data.user.repository.AppStateRepositoryImpl
import com.profpay.data.user.repository.UserRepositoryImpl
import com.profpay.domain.user.repository.AppStateRepository
import com.profpay.domain.user.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserDataModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAppStateRepository(impl: AppStateRepositoryImpl): AppStateRepository

    companion object {

        /**
         * Публичный API — без авторизации кошельком.
         * Используется для: регистрации, проверки разрешений, принятия согласия.
         */
        @Provides
        @Singleton
        fun providePublicUserApi(factory: RetrofitFactory): PublicUserApi =
            factory.createPublicApi(PublicUserApi::class.java)

        /**
         * Защищённый API — с авторизацией кошельком.
         * Используется для: операций с Telegram, проверки существования пользователя.
         */
        @Provides
        @Singleton
        fun provideUserApi(factory: RetrofitFactory): UserApi =
            factory.createAuthenticatedApi(UserApi::class.java)
    }
}
