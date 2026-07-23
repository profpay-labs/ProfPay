package com.profpay.data.wallet.mapper

import com.profpay.core.database.entities.wallet.CentralAddressEntity
import com.profpay.domain.wallet.model.local.CentralAddressLocal

object CentralAddressMapper {

    fun CentralAddressEntity.toLocal(): CentralAddressLocal = CentralAddressLocal(
        id = centralId ?: 0L,
        address = address,
        publicKey = publicKey,
        privateKey = privateKey,
        trxBalance = balance,
    )

    fun CentralAddressLocal.toEntity(): CentralAddressEntity = CentralAddressEntity(
        centralId = id,
        address = address,
        publicKey = publicKey,
        privateKey = privateKey,
        balance = trxBalance,
    )
}
