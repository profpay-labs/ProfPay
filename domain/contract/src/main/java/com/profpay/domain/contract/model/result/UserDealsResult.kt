package com.profpay.domain.contract.model.result

import com.profpay.domain.contract.model.Deal

data class UserDealsResult(
    val deals: List<Deal>,
    val timestampSeconds: Long,
)
