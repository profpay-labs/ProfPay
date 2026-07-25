package com.profpay.core.network.di

import com.profpay.core.network.BuildConfig
import com.profpay.core.network.interceptor.LoggingInterceptor
import com.profpay.core.network.interceptor.RequestMetadataInterceptor
import com.profpay.core.network.interceptor.WalletSignatureInterceptor
import com.profpay.core.network.qualifier.AuthenticatedClient
import com.profpay.core.network.qualifier.BaseUrl
import com.profpay.core.network.qualifier.PublicClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    /**
     * Certificate Pinner для production.
     * Дополнительная защита помимо Network Security Config.
     */
    private val certificatePinner: CertificatePinner = CertificatePinner.Builder()
        // ProfPay API
        .add(
            "api.profpay-services.com",
            "sha256/CK1Mp5hVBN7kGViots8TwMypArWsCLW6qknKdov0Mlo=",
            "sha256/LoMHBotttiDko50Gi13uXW71eIy7LAttI+rYT8wXF4w="
        )
        .add(
            "*.profpay-services.com",
            "sha256/CK1Mp5hVBN7kGViots8TwMypArWsCLW6qknKdov0Mlo=",
            "sha256/LoMHBotttiDko50Gi13uXW71eIy7LAttI+rYT8wXF4w="
        )
        // Sentry
        .add(
            "sentry.profpay-services.com",
            "sha256/sDWnGJvKEdLs8ESkX5lKRa+JGz9Fx2rG9OQYGz3jt7o=",
            "sha256/nWN7PSep5XDQdge5zK24CnCRXHr3KvzhKEGxsdqCX9E="
        )
        .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideConverterFactory(json: Json): Converter.Factory =
        json.asConverterFactory("application/json".toMediaType())

    /**
     * OkHttpClient с авторизацией — для защищённых endpoints
     */
    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedOkHttpClient(
        walletSignatureInterceptor: WalletSignatureInterceptor,
        requestMetadataInterceptor: RequestMetadataInterceptor,
        loggingInterceptor: LoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(walletSignatureInterceptor)
        .addInterceptor(requestMetadataInterceptor)
        .apply {
            if (BuildConfig.DEBUG_LOGGING) {
                addInterceptor(loggingInterceptor)
            } else {
                // Certificate pinning только в release
                certificatePinner(certificatePinner)
            }
        }
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * OkHttpClient без авторизации — для публичных endpoints (регистрация, refresh token)
     */
    @Provides
    @Singleton
    @PublicClient
    fun providePublicOkHttpClient(
        loggingInterceptor: LoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG_LOGGING) {
                addInterceptor(loggingInterceptor)
            } else {
                certificatePinner(certificatePinner)
            }
        }
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Retrofit.Builder — базовый builder, который data модули используют для создания API
     */
    @Provides
    @Singleton
    fun provideRetrofitBuilder(
        converterFactory: Converter.Factory,
    ): Retrofit.Builder = Retrofit.Builder()
        .addConverterFactory(converterFactory)

    /**
     * Base URL для API
     */
    @Provides
    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = BuildConfig.BASE_URL
}
