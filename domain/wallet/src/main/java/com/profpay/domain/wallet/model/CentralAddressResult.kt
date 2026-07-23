package com.profpay.domain.wallet.model

data class CentralAddressResult(
    val id: Long,
    val userId: Long,
    val address: String,
    val publicKey: String,
    val type: AddressType,
    val active: Boolean,
)
