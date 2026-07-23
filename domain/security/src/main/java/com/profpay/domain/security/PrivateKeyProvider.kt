package com.profpay.domain.security

import com.profpay.domain.wallet.model.local.WalletAddressLocal

/**
 * Провайдер приватных ключей для подписания транзакций.
 *
 * Абстрагирует логику получения ключей от конкретной реализации
 * (Keystore, BIP derivation и т.д.)
 */
interface PrivateKeyProvider {

    /**
     * Возвращает приватный ключ в виде ByteArray.
     * ВАЖНО: Caller ДОЛЖЕН обнулить массив после использования!
     */
    suspend fun resolve(walletAddress: WalletAddressLocal): ByteArray

    /**
     * Возвращает приватный ключ в hex-формате.
     */
    suspend fun resolveHex(walletAddress: WalletAddressLocal): String
}
