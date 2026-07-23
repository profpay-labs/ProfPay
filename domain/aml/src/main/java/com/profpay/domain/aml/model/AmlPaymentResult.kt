package com.profpay.domain.aml.model

/**
 * Результат создания AML платежа
 */
data class AmlPaymentResult(
    val operationId: Long,
    val status: PaymentStatus,
    val timestampSeconds: Long,
)
