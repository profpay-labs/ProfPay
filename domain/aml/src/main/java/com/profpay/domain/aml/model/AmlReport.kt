package com.profpay.domain.aml.model

data class AmlReport(
    val id: Long,
    val amlId: String,
    val riskScore: Double,
    val status: AmlStatus,
    val signals: List<AmlSignal>,
    val createdAtSeconds: Long,
    val requestedAtSeconds: Long = 0,
) {
    val isHighRisk: Boolean get() = riskScore >= 70
    val isMediumRisk: Boolean get() = riskScore in 40.0..69.0
    val isLowRisk: Boolean get() = riskScore < 40
    val isPending: Boolean get() = status == AmlStatus.PENDING
}
