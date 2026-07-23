package com.profpay.data.aml.mapper

import com.profpay.core.database.entities.wallet.PendingAmlTransactionEntity
import com.profpay.domain.aml.model.local.PendingAmlStatus
import com.profpay.domain.aml.model.local.PendingAmlTransactionLocal

object PendingAmlTransactionMapper {

    fun PendingAmlTransactionEntity.toLocal(): PendingAmlTransactionLocal = PendingAmlTransactionLocal(
        id = id,
        txId = txid,
        status = when {
            isSuccessful -> PendingAmlStatus.SUCCESS
            isError -> PendingAmlStatus.ERROR
            else -> PendingAmlStatus.PENDING
        },
    )

    fun PendingAmlTransactionLocal.toEntity(): PendingAmlTransactionEntity = PendingAmlTransactionEntity(
        id = id,
        txid = txId,
        isSuccessful = status == PendingAmlStatus.SUCCESS,
        isError = status == PendingAmlStatus.ERROR,
    )
}
