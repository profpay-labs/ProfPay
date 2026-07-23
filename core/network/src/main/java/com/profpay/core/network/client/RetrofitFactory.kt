package com.profpay.core.network.client

import com.profpay.core.network.qualifier.AuthenticatedClient
import com.profpay.core.network.qualifier.BaseUrl
import com.profpay.core.network.qualifier.PublicClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Фабрика для создания Retrofit API интерфейсов.
 * Используется в data модулях для создания конкретных API.
 */
@Singleton
class RetrofitFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder,
    @AuthenticatedClient private val authenticatedClient: OkHttpClient,
    @PublicClient private val publicClient: OkHttpClient,
    @BaseUrl private val baseUrl: String,
) {

    /**
     * Создаёт API с авторизацией
     */
    fun <T> createAuthenticatedApi(apiClass: Class<T>): T =
        createApi(apiClass, authenticatedClient, baseUrl)

    /**
     * Создаёт API с авторизацией и кастомным base URL
     */
    fun <T> createAuthenticatedApi(apiClass: Class<T>, customBaseUrl: String): T =
        createApi(apiClass, authenticatedClient, customBaseUrl)

    /**
     * Создаёт публичный API (без авторизации)
     */
    fun <T> createPublicApi(apiClass: Class<T>): T =
        createApi(apiClass, publicClient, baseUrl)

    /**
     * Создаёт публичный API с кастомным base URL
     */
    fun <T> createPublicApi(apiClass: Class<T>, customBaseUrl: String): T =
        createApi(apiClass, publicClient, customBaseUrl)

    private fun <T> createApi(
        apiClass: Class<T>,
        client: OkHttpClient,
        url: String,
    ): T = retrofitBuilder
        .baseUrl(url)
        .client(client)
        .build()
        .create(apiClass)
}
