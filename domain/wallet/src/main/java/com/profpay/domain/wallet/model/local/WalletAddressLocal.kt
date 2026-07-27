package com.profpay.domain.wallet.model.local

/**
 * Локальная модель адреса кошелька для работы с БД.
 * Содержит все поля, необходимые для сохранения/чтения из Room.
 */
data class WalletAddressLocal(
    val id: Long = 0L,
    val walletId: Long,
    val blockchainName: String,
    val address: String,
    val publicKey: String,
    val isGeneralAddress: Boolean,
    val sotIndex: Byte,
    val sotDerivationIndex: Int,
)
