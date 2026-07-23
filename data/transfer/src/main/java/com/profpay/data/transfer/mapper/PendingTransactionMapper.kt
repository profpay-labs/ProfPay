package com.profpay.data.transfer.mapper

import com.profpay.core.database.entities.wallet.PendingTransactionEntity
import com.profpay.domain.transfer.model.local.PendingTransactionLocal

object PendingTransactionMapper {

    fun PendingTransactionEntity.toLocal(): PendingTransactionLocal = PendingTransactionLocal(
        id = id,
        tokenId = tokenId,
        txId = txid,
        amount = amount,
        timestamp = timestamp,
        ttlMillis = ttlMillis,
    )

    fun PendingTransactionLocal.toEntity(): PendingTransactionEntity = PendingTransactionEntity(
        id = id,
        tokenId = tokenId,
        txid = txId,
        amount = amount,
        timestamp = timestamp,
        ttlMillis = ttlMillis,
    )
}
