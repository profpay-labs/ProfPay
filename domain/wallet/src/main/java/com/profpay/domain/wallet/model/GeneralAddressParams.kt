package com.profpay.domain.wallet.model

data class GeneralAddressParams(
    val address: String,
    val pubKey: String,
    val derivedIndices: List<Int>,
)
