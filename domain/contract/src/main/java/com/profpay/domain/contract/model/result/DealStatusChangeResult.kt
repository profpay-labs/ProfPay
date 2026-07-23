package com.profpay.domain.contract.model.result

/**
 * Результат изменения статуса сделки
 */
data class DealStatusChangeResult(
    /** ID сделки */
    val dealId: Long,
    /** Новый статус сделки */
    val newStatus: String,
    /** Покупатель уведомлён */
    val buyerNotified: Boolean,
    /** Продавец уведомлён */
    val sellerNotified: Boolean,
    /** Timestamp обработки */
    val timestamp: Long,
)
