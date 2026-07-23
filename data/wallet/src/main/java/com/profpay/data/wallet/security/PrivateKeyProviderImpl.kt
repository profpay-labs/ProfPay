package com.profpay.data.wallet.security

import com.profpay.core.security.CryptoManager
import com.profpay.core.tron.Tron
import com.profpay.domain.security.PrivateKeyProvider
import com.profpay.domain.wallet.model.local.WalletAddressLocal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.WalletProfileLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация провайдера приватных ключей.
 *
 * Использует:
 * - Android Keystore для расшифровки entropy
 * - BIP derivation через Tron SDK для получения ключа
 */
@Singleton
class PrivateKeyProviderImpl @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val walletProfileLocalRepository: WalletProfileLocalRepository,
    private val cryptoManager: CryptoManager,
    private val tron: Tron,
) : PrivateKeyProvider {

    override suspend fun resolve(walletAddress: WalletAddressLocal): ByteArray {
        val generalAddress = addressLocalRepository.getGeneralAddressByWalletId(walletAddress.walletId)
        val cipherData = walletProfileLocalRepository.getCipherData(walletAddress.walletId)

        val entropy = cryptoManager.decrypt(
            alias = generalAddress,
            iv = cipherData.iv,
            cipherText = cipherData.cipherText,
        )

        return tron.addressUtilities.derivePrivateKeyFromEntropy(
            entropy,
            walletAddress.sotDerivationIndex,
        )
    }

    override suspend fun resolveHex(walletAddress: WalletAddressLocal): String {
        val generalAddress = addressLocalRepository.getGeneralAddressByWalletId(walletAddress.walletId)
        val cipherData = walletProfileLocalRepository.getCipherData(walletAddress.walletId)

        val entropy = cryptoManager.decrypt(
            alias = generalAddress,
            iv = cipherData.iv,
            cipherText = cipherData.cipherText,
        )

        return tron.addressUtilities.deriveHexPrivateKeyFromEntropy(
            entropy,
            walletAddress.sotDerivationIndex,
        )
    }
}
