package com.profpay.domain.wallet.model.local

import java.math.BigInteger

/**
 * Локальная модель центрального адреса.
 */
data class CentralAddressLocal(
    val id: Long = 0L,
    val address: String,
    val publicKey: String,
    val privateKey: String,
    val trxBalance: BigInteger = BigInteger.ZERO,
)
