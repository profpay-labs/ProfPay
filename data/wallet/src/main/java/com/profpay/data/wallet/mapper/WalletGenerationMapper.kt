package com.profpay.data.wallet.mapper

import com.profpay.core.tron.model.AddressGenerateFromSeedPhr
import com.profpay.core.tron.model.AddressGenerateResult
import com.profpay.core.tron.model.AddressesWithKeysForM
import com.profpay.domain.wallet.model.GeneratedWalletData
import com.profpay.domain.wallet.model.RecoveredAddressData

/**
 * Маппер для конвертации моделей core:tron в domain модели.
 */
object WalletGenerationMapper {

    fun toGeneratedWalletData(source: AddressGenerateResult): GeneratedWalletData {
        return GeneratedWalletData(
            mnemonicPhrase = source.mnemonicPhrase,
            mnemonicWords = source.mnemonicWords,
            entropy = source.addressesWithKeysForM.entropy,
            addresses = source.addressesWithKeysForM.addresses.map { addr ->
                GeneratedWalletData.GeneratedAddress(
                    address = addr.address,
                    publicKey = addr.publicKey,
                    sotIndex = addr.indexSot.toInt(),
                    derivationIndex = addr.indexDerivationSot,
                )
            }
        )
    }

    fun toRecoveredAddressData(source: AddressGenerateFromSeedPhr): RecoveredAddressData {
        return RecoveredAddressData(
            entropy = source.addressesWithKeysForM.entropy,
            addresses = source.addressesWithKeysForM.addresses.map { addr ->
                RecoveredAddressData.AddressInfo(
                    address = addr.address,
                    publicKey = addr.publicKey,
                    sotIndex = addr.indexSot.toInt(),
                    derivationIndex = addr.indexDerivationSot,
                )
            }
        )
    }

    fun toRecoveredAddressData(source: AddressesWithKeysForM): RecoveredAddressData {
        return RecoveredAddressData(
            entropy = source.entropy,
            addresses = source.addresses.map { addr ->
                RecoveredAddressData.AddressInfo(
                    address = addr.address,
                    publicKey = addr.publicKey,
                    sotIndex = addr.indexSot.toInt(),
                    derivationIndex = addr.indexDerivationSot,
                )
            }
        )
    }
}
