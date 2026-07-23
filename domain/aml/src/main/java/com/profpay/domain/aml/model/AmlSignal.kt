package com.profpay.domain.aml.model

/**
 * Сигнал риска из AML отчёта
 */
data class AmlSignal(
    val name: String,
    val percentage: Double,
    val riskLevel: RiskLevel,
)
