package com.profpay.data.market.mapper

import com.profpay.core.database.entities.wallet.ExchangeRatesEntity
import com.profpay.domain.market.model.local.ExchangeRateLocal

object ExchangeRatesMapper {

    fun ExchangeRatesEntity.toLocal(): ExchangeRateLocal = ExchangeRateLocal(
        id = id,
        symbol = symbol,
        rate = value,
    )

    fun ExchangeRateLocal.toEntity(): ExchangeRatesEntity = ExchangeRatesEntity(
        id = id,
        symbol = symbol,
        value = rate,
    )
}
