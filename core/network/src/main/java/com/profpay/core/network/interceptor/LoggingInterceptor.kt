package com.profpay.core.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor для логирования HTTP запросов/ответов.
 * Автоматически скрывает sensitive данные.
 */
@Singleton
class LoggingInterceptor @Inject constructor() : Interceptor {

    private val delegate = HttpLoggingInterceptor { message ->
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    override fun intercept(chain: Interceptor.Chain): Response =
        delegate.intercept(chain)

    /**
     * Убирает sensitive данные из логов
     */
    private fun sanitize(message: String): String {
        var sanitized = message

        SENSITIVE_PATTERNS.forEach { (pattern, replacement) ->
            sanitized = sanitized.replace(pattern, replacement)
        }

        return sanitized
    }

    companion object {
        private const val TAG = "NetworkLog"

        private val SENSITIVE_PATTERNS = listOf(
            Regex(""""access_token"\s*:\s*"[^"]+"""") to """"access_token":"***REDACTED***"""",
            Regex(""""refresh_token"\s*:\s*"[^"]+"""") to """"refresh_token":"***REDACTED***"""",
            Regex(""""password"\s*:\s*"[^"]+"""") to """"password":"***REDACTED***"""",
            Regex(""""private_key"\s*:\s*"[^"]+"""") to """"private_key":"***REDACTED***"""",
            Regex(""""seed_phrase"\s*:\s*"[^"]+"""") to """"seed_phrase":"***REDACTED***"""",
            Regex(""""mnemonic"\s*:\s*"[^"]+"""") to """"mnemonic":"***REDACTED***"""",
            Regex("""Authorization:\s*Bearer\s+\S+""") to "Authorization: Bearer ***REDACTED***",
            // Добавить для новой схемы
            Regex("""X-Wallet-Signature:\s*\S+""") to "X-Wallet-Signature: ***REDACTED***",
            Regex("""X-Wallet-Public-Key:\s*\S+""") to "X-Wallet-Public-Key: ***REDACTED***",
        )
    }
}
