package com.profpay.data.aml.mapper

import com.profpay.data.aml.dto.AmlPaymentResponseDto
import com.profpay.data.aml.dto.AmlReportDto
import com.profpay.data.aml.dto.AmlSignalDto
import com.profpay.domain.aml.model.AmlPaymentResult
import com.profpay.domain.aml.model.AmlReport
import com.profpay.domain.aml.model.AmlSignal
import com.profpay.domain.aml.model.AmlStatus
import com.profpay.domain.aml.model.PaymentStatus
import com.profpay.domain.aml.model.RiskLevel

internal fun AmlReportDto.toDomain(): AmlReport = AmlReport(
    id = id,
    amlId = amlId,
    riskScore = riskScore,
    status = AmlStatus.fromString(status),
    signals = signals.map { it.toDomain() },
    createdAtSeconds = createdAt,
    requestedAtSeconds = requestedAt,
)

internal fun AmlSignalDto.toDomain(): AmlSignal = AmlSignal(
    name = name,
    percentage = percentage,
    riskLevel = RiskLevel.fromString(riskLevel),
)

internal fun AmlPaymentResponseDto.toDomain(): AmlPaymentResult = AmlPaymentResult(
    operationId = operationId,
    status = PaymentStatus.fromString(status),
    timestampSeconds = timestamp,
)
