package com.profpay.core.tron.network

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * HTTP клиент для работы с TronGrid API.
 * Thread-safe, использует OkHttp с настроенными таймаутами.
 */
@Singleton
class TronGridClient @Inject constructor() {

    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @PublishedApi
    internal val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Выполняет GET запрос и десериализует ответ.
     */
    suspend inline fun <reified T> get(
        crossinline urlBuilder: HttpUrl.Builder.() -> Unit,
    ): T = suspendCancellableCoroutine { continuation ->
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(TRON_GRID_HOST)
            .apply { urlBuilder() }
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()

        val call = client.newCall(request)

        continuation.invokeOnCancellation {
            call.cancel()
        }

        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        TronGridException("Network error: ${e.message}", e)
                    )
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!continuation.isActive) return

                response.use { resp ->
                    if (!resp.isSuccessful) {
                        continuation.resumeWithException(
                            TronGridException(
                                "HTTP ${resp.code}: ${resp.message}",
                                null,
                                resp.code
                            )
                        )
                        return
                    }

                    val body = resp.body?.string()
                    if (body.isNullOrEmpty()) {
                        continuation.resumeWithException(
                            TronGridException("Empty response body")
                        )
                        return
                    }

                    try {
                        val result = json.decodeFromString<T>(body)
                        continuation.resume(result)
                    } catch (e: Exception) {
                        continuation.resumeWithException(
                            TronGridException("JSON parsing error: ${e.message}", e)
                        )
                    }
                }
            }
        })
    }

    companion object {
        @PublishedApi
        internal const val TRON_GRID_HOST = "api.trongrid.io"

        private const val CONNECT_TIMEOUT_SECONDS = 30L
        private const val READ_TIMEOUT_SECONDS = 30L
        private const val WRITE_TIMEOUT_SECONDS = 30L
    }
}

/**
 * Исключение для ошибок TronGrid API.
 */
class TronGridException(
    message: String,
    cause: Throwable? = null,
    val httpCode: Int? = null,
) : Exception(message, cause)
