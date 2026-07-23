package com.profpay.domain.aml.exception

/**
 * Специфичные ошибки AML модуля
 */
sealed class AmlError : Exception() {

    data class ReportNotFound(
        val txHash: String,
    ) : AmlError() {
        override val message: String = "AML report not found for transaction: $txHash"
    }

    data class ProviderUnavailable(
        override val cause: Throwable? = null,
    ) : AmlError() {
        override val message: String = "AML provider is temporarily unavailable"
    }

    data class InvalidTransaction(
        val txHash: String,
        val reason: String,
    ) : AmlError() {
        override val message: String = "Invalid transaction $txHash: $reason"
    }

    /**
     * Слишком частые запросы на обновление (cooldown не истёк)
     */
    data class RenewCooldownNotExpired(
        val txHash: String,
    ) : AmlError() {
        override val message: String = "Cannot renew report for $txHash: 24h cooldown not expired"
    }

    /**
     * Некорректные данные запроса на платёж
     */
    data class InvalidPaymentRequest(
        val reason: String,
    ) : AmlError() {
        override val message: String = "Invalid payment request: $reason"
    }

    /**
     * Ошибка при покупке ресурсов (bandwidth)
     */
    data class ResourcePurchaseFailed(
        override val cause: Throwable? = null,
    ) : AmlError() {
        override val message: String = "Failed to purchase bandwidth resources"
    }
}
