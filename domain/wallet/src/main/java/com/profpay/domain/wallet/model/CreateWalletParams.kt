package com.profpay.domain.wallet.model

/**
 * Параметры для создания кошелька
 */
data class CreateWalletParams(
    val appId: String,
    val generalAddress: GeneralAddressParams,
    val centralAddress: CentralAddressParams,
    val sotAddresses: List<SotAddressParams>,
)
