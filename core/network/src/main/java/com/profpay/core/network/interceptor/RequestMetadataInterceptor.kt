package com.profpay.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor для добавления общих заголовков:
 * - Request ID для трейсинга
 * - Timestamp
 * - Platform info
 */
@Singleton
class RequestMetadataInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .header(HEADER_REQUEST_ID, UUID.randomUUID().toString())
            .header(HEADER_PLATFORM, PLATFORM_ANDROID)
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
            .build()

        return chain.proceed(newRequest)
    }

    companion object {
        private const val HEADER_REQUEST_ID = "X-Request-ID"
        private const val HEADER_PLATFORM = "X-Platform"
        private const val HEADER_ACCEPT = "Accept"

        private const val PLATFORM_ANDROID = "Android"
        private const val CONTENT_TYPE_JSON = "application/json"
    }
}
