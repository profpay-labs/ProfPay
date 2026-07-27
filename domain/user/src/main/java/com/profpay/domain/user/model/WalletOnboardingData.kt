package com.profpay.domain.user.model

/**
 * Данные кошелька для онбординга.
 */
data class WalletOnboardingData(
    val generalAddress: GeneralAddress,
    val centralAddress: CentralAddress? = null,
    val sotAddresses: List<SotAddress> = emptyList(),
)

/**
 * Основной адрес кошелька.
 */
data class GeneralAddress(
    val address: String,
    val pubKey: String,
    val derivedIndices: List<Int>,
)

/**
 * Центральный адрес кошелька.
 */
data class CentralAddress(
    val address: String,
    val pubKey: String,
)

/**
 * SOT адрес кошелька.
 */
data class SotAddress(
    val address: String,
    val pubKey: String,
    val index: Byte,
    val derivationIndex: Int,
)
