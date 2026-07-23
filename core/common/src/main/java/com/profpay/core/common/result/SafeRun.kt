package com.profpay.core.common.result

import io.sentry.Sentry

/**
 * Безопасно выполняет блок кода, перехватывая исключения.
 *
 * @param logToSentry true — логировать в Sentry (по умолчанию)
 * @param block блок кода для выполнения
 * @return результат или null при исключении
 */
inline fun <T> safeRun(
    logToSentry: Boolean = true,
    block: () -> T,
): T? = try {
    block()
} catch (e: Exception) {
    if (logToSentry) {
        Sentry.captureException(e)
    }
    null
}

/**
 * Безопасно выполняет suspend-блок кода.
 */
suspend inline fun <T> safeSuspendRun(
    logToSentry: Boolean = true,
    crossinline block: suspend () -> T,
): T? = try {
    block()
} catch (e: Exception) {
    if (logToSentry) {
        Sentry.captureException(e)
    }
    null
}

/**
 * Преобразует блок в Result с автоматическим логированием ошибок в Sentry.
 */
inline fun <T> runCatchingWithSentry(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: Exception) {
        Sentry.captureException(e)
        Result.failure(e)
    }
