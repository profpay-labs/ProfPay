package com.profpay.domain.wallet.model

data class WalletAddress(
    val id: Long,
    val address: String,
    val publicKey: String,
    val type: AddressType,
    val derivedIndices: List<Int>,
    val sot: SotInfo?,
    val parentAddressId: Long?,
    val active: Boolean,
)
