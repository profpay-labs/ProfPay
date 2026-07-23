package com.profpay.data.contract.mapper

import com.profpay.core.database.entities.wallet.SmartContractEntity
import com.profpay.domain.contract.model.local.SmartContractLocal

object SmartContractMapper {

    fun SmartContractEntity.toLocal(): SmartContractLocal = SmartContractLocal(
        id = id,
        contractAddress = contractAddress,
        ownerAddress = ownerAddress,
        openDealsCount = openDealsCount ?: 0,
        closedDealsCount = closedDealsCount ?: 0,
    )

    fun SmartContractLocal.toEntity(): SmartContractEntity = SmartContractEntity(
        id = id,
        contractAddress = contractAddress,
        ownerAddress = ownerAddress,
        openDealsCount = openDealsCount,
        closedDealsCount = closedDealsCount,
    )
}
