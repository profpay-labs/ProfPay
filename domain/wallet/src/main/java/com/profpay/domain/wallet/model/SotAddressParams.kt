package com.profpay.domain.wallet.model

data class SotAddressParams(
    val address: String,
    val pubKey: String,
    val index: Byte,
    val derivationIndex: Int,
)
