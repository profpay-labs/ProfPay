package com.profpay.wallet.ui.screens.wallet.aml

import com.profpay.domain.aml.model.AmlReport
import com.profpay.wallet.ui.screens.wallet.AMLType

/**
 * Определяет UI-тип риска на основе riskScore из AML-отчёта.
 */
fun AmlReport.toAmlType(): AMLType = when {
    riskScore >= HIGH_RISK_THRESHOLD -> AMLType.HIGH_RISC
    riskScore >= MEDIUM_RISK_THRESHOLD -> AMLType.MEDIUM_RISC
    else -> AMLType.LOW_RISC
}

private const val HIGH_RISK_THRESHOLD = 70.0
private const val MEDIUM_RISK_THRESHOLD = 50.0
