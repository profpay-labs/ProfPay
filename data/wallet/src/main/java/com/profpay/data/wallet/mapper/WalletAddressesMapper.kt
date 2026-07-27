package com.profpay.data.wallet.mapper

import com.profpay.core.tron.model.AddressesWithKeysForM
import com.profpay.domain.wallet.model.WalletAddressesData

/**
 * Маппер между core:tron моделью и domain моделью.
 */
object WalletAddressesMapper {

    fun toWalletAddressesData(source: AddressesWithKeysForM): WalletAddressesData {
        return WalletAddressesData(
            entropy = source.entropy,
            addresses = source.addresses.map { addr ->
                WalletAddressesData.AddressData(
                    address = addr.address,
                    publicKey = addr.publicKey,
                    sotIndex = addr.indexSot,
                    derivationIndex = addr.indexDerivationSot,
                )
            }
        )
    }
}
