package com.profpay.core.database.models

import androidx.room.Embedded
import androidx.room.Relation
import com.profpay.core.database.entities.wallet.PendingTransactionEntity
import com.profpay.core.database.entities.wallet.TokenEntity
import java.math.BigInteger

data class TokenWithPendingTransactions(
    @Embedded val token: TokenEntity,
    @Relation(
        parentColumn = "token_id",
        entityColumn = "token_id",
    )
    val pendingTransactions: List<PendingTransactionEntity>,
) {
    val frozenBalance: BigInteger
        get() = pendingTransactions.sumOf { it.amount }
    val balanceWithoutFrozen: BigInteger
        get() = (token.balance - frozenBalance).coerceAtLeast(BigInteger.ZERO)
}
