package com.profpay.wallet.presentation.viewmodel.smartcontract

import com.profpay.domain.contract.model.Deal

enum class SmartContractButtonType {
    ACCEPT,
    REJECT,
}

/**
 * UI-состояние модального окна экрана смарт-контрактов.
 * Либо прогресс операции (text), либо подтверждение действия (buttonType + deal).
 */
data class SmartContractModalData(
    val isActive: Boolean,
    val text: String,
    val buttonType: SmartContractButtonType? = null,
    val deal: Deal? = null,
) {
    companion object {
        val Hidden = SmartContractModalData(isActive = false, text = "")
    }
}

data class EstimateResourcePriceResult(
    val commission: Long,
)
