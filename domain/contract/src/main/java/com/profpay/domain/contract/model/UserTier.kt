package com.profpay.domain.contract.model

data class UserTier(
    val name: String,
    val code: String,
    val commissionPercent: Int,
)
