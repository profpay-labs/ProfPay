package com.profpay.domain.wallet.model

/**
 * Параметры для добавления дополнительного кошелька.
 */
data class AddWalletParams(
    val generalAddress: GeneralAddressParams,
    val sotAddresses: List<SotAddressParams> = emptyList(),
)
