package com.profpay.core.network.exception

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Безопасный вызов API с маппингом ошибок в NetworkError
 */
suspend fun <T> safeApiCall(
    json: Json = Json { ignoreUnknownKeys = true },
    apiCall: suspend () -> Response<T>,
): Result<T> = try {
    val response = apiCall()

    if (response.isSuccessful) {
        val body = response.body()
        if (body != null) {
            Result.success(body)
        } else {
            Result.failure(
                NetworkError.HttpError(
                    code = response.code(),
                    errorBody = null,
                    message = "Empty response body"
                )
            )
        }
    } else {
        val errorBody = response.errorBody()?.string()

        // Пробуем распарсить ошибку как ApiErrorResponse
        val apiError = errorBody?.let {
            try {
                json.decodeFromString<ApiErrorResponse>(it)
            } catch (_: Exception) {
                null
            }
        }

        if (apiError != null && (apiError.message != null || apiError.error != null)) {
            Result.failure(ApiException.fromResponse(apiError, httpCode = response.code()))
        } else {
            Result.failure(
                NetworkError.HttpError(
                    code = response.code(),
                    errorBody = errorBody,
                )
            )
        }
    }
} catch (e: CancellationException) {
    throw e // Не перехватываем cancellation
} catch (e: SocketTimeoutException) {
    Result.failure(NetworkError.TimeoutError(e))
} catch (e: UnknownHostException) {
    Result.failure(NetworkError.ConnectionError(e))
} catch (e: SSLException) {
    Result.failure(NetworkError.ConnectionError(e))
} catch (e: kotlinx.serialization.SerializationException) {
    Result.failure(NetworkError.ParseError(e))
} catch (e: Exception) {
    Result.failure(NetworkError.UnknownError(e))
}

/**
 * Extension для Result с автоматическим retry
 */
suspend fun <T> safeApiCallWithRetry(
    maxRetries: Int = 3,
    delayMillis: Long = 1000,
    shouldRetry: (Throwable) -> Boolean = { it is NetworkError.ConnectionError || it is NetworkError.TimeoutError },
    apiCall: suspend () -> Response<T>,
): Result<T> {
    var lastError: Throwable? = null

    repeat(maxRetries) { attempt ->
        val result = safeApiCall(apiCall = apiCall)

        result.onSuccess { return Result.success(it) }
        result.onFailure { error ->
            lastError = error
            if (!shouldRetry(error) || attempt == maxRetries - 1) {
                return Result.failure(error)
            }
            kotlinx.coroutines.delay(delayMillis * (attempt + 1))
        }
    }

    return Result.failure(lastError ?: NetworkError.UnknownError(Exception("Max retries reached")))
}
