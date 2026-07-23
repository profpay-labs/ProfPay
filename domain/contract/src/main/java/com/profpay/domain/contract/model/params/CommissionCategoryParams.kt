package com.profpay.domain.contract.model.params

import com.profpay.domain.contract.model.CommissionType

data class CommissionCategoryParams(
    val type: CommissionType,
    val amount: Long,
    val description: String,
)
