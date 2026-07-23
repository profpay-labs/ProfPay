package com.profpay.data.wallet.mapper

import com.profpay.core.database.entities.wallet.TokenEntity
import com.profpay.domain.wallet.model.local.TokenLocal
import java.math.BigInteger

object TokenMapper {

    fun TokenEntity.toLocal(): TokenLocal = TokenLocal(
        id = tokenId,
        addressId = addressId,
        tokenName = tokenName,
        balance = balance,
        frozenBalance = frozenBalance ?: BigInteger.ZERO,
    )

    fun TokenLocal.toEntity(): TokenEntity = TokenEntity(
        tokenId = id,
        addressId = addressId,
        tokenName = tokenName,
        balance = balance,
        frozenBalance = frozenBalance,
    )
}
