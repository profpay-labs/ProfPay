package com.profpay.domain.wallet.model

/**
 * Параметры для обновления derived index SOT-адреса
 */
data class UpdateDerivedIndexParams(
    val appId: String,
    val oldSotAddress: String,
    val newSotAddress: SotAddressParams,
    val generalAddress: GeneralAddressUpdateParams,
)
