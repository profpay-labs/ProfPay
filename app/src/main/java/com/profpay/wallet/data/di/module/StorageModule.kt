package com.profpay.wallet.data.di.module

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.profpay.wallet.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
         return context.getSharedPreferences(
             context.getString(R.string.preference_file_key),
             MODE_PRIVATE
         )
    }
}
