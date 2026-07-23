package com.profpay.domain.wallet.model

data class GeneralAddressUpdateParams(
    val address: String,
    val oldSotDerivationIndex: Int,
    val newSotDerivationIndex: Int,
)
