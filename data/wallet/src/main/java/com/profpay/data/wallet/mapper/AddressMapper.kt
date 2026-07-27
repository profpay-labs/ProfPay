package com.profpay.data.wallet.mapper

import com.profpay.core.database.entities.wallet.AddressEntity
import com.profpay.core.database.models.AddressWithTokens
import com.profpay.core.database.models.TokenWithPendingTransactions
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.domain.wallet.model.local.TokenBalanceLocal
import com.profpay.domain.wallet.model.local.WalletAddressLocal

object AddressMapper {

    fun AddressEntity.toLocal(): WalletAddressLocal = WalletAddressLocal(
        id = addressId ?: 0L,
        walletId = walletId,
        blockchainName = blockchainName,
        address = address,
        publicKey = publicKey,
        isGeneralAddress = isGeneralAddress,
        sotIndex = sotIndex,
        sotDerivationIndex = sotDerivationIndex,
    )

    fun WalletAddressLocal.toEntity(): AddressEntity = AddressEntity(
        addressId = if (id == 0L) null else id,
        walletId = walletId,
        blockchainName = blockchainName,
        address = address,
        publicKey = publicKey,
        isGeneralAddress = isGeneralAddress,
        sotIndex = sotIndex,
        sotDerivationIndex = sotDerivationIndex,
    )

    fun AddressWithTokens.toLocal(): AddressWithTokensLocal = AddressWithTokensLocal(
        address = addressEntity.toLocal(),
        tokens = tokens.map { it.toLocal() },
    )

    fun TokenWithPendingTransactions.toLocal(): TokenBalanceLocal = TokenBalanceLocal(
        tokenId = token.tokenId ?: 0L,
        tokenName = token.tokenName,
        balance = token.balance,
        frozenBalance = frozenBalance,
    )
}
