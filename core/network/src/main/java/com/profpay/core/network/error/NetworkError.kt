package com.profpay.core.network.error

sealed class NetworkError : Exception() {

    /**
     * HTTP ошибка с кодом статуса
     */
    data class HttpError(
        val code: Int,
        val errorBody: String?,
        override val message: String = "HTTP Error $code",
    ) : NetworkError() {

        val isClientError: Boolean get() = code in 400..499
        val isServerError: Boolean get() = code in 500..599
        val isUnauthorized: Boolean get() = code == 401
        val isForbidden: Boolean get() = code == 403
        val isNotFound: Boolean get() = code == 404
        val isTooManyRequests: Boolean get() = code == 429
        val isConflict: Boolean get() = code == 409
        val isBadRequest: Boolean get() = code == 400
    }

    /**
     * Ошибка подключения (нет интернета, DNS не резолвится)
     */
    data class ConnectionError(
        override val cause: Throwable,
        override val message: String = "Connection failed: ${cause.message}",
    ) : NetworkError()

    /**
     * Таймаут запроса
     */
    data class TimeoutError(
        override val cause: Throwable,
        override val message: String = "Request timed out",
    ) : NetworkError()

    /**
     * Ошибка парсинга ответа
     */
    data class ParseError(
        override val cause: Throwable,
        override val message: String = "Failed to parse response: ${cause.message}",
    ) : NetworkError()

    /**
     * Неизвестная ошибка
     */
    data class UnknownError(
        override val cause: Throwable,
        override val message: String = "Unknown error: ${cause.message}",
    ) : NetworkError()
}
