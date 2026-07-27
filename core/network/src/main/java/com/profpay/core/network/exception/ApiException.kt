package com.profpay.core.network.exception

import kotlinx.serialization.Serializable

/**
 * Стандартная модель ошибки от API
 */
@Serializable
data class ApiErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val code: String? = null,
    val details: Map<String, String>? = null,
)

/**
 * Exception для бизнес-ошибок API
 */
class ApiException(
    val errorCode: String?,
    override val message: String,
    val httpCode: Int? = null,  // ← Добавить
    val details: Map<String, String>? = null,
) : Exception(message) {

    val isNotFound: Boolean get() = httpCode == 404
    val isBadRequest: Boolean get() = httpCode == 400
    val isTooManyRequests: Boolean get() = httpCode == 429

    companion object {
        fun fromResponse(response: ApiErrorResponse, httpCode: Int? = null): ApiException =
            ApiException(
                errorCode = response.code,
                message = response.message ?: response.error ?: "Unknown API error",
                httpCode = httpCode,
                details = response.details,
            )
    }
}
