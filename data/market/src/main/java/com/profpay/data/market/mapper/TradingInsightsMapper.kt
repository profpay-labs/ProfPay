package com.profpay.data.market.mapper

import com.profpay.core.database.entities.wallet.TradingInsightsEntity
import com.profpay.domain.market.model.local.TradingInsightsLocal

object TradingInsightsMapper {

    fun TradingInsightsEntity.toLocal(): TradingInsightsLocal = TradingInsightsLocal(
        id = id,
        symbol = symbol,
        priceChangePercentage24h = priceChangePercentage24h,
    )

    fun TradingInsightsLocal.toEntity(): TradingInsightsEntity = TradingInsightsEntity(
        id = id,
        symbol = symbol,
        priceChangePercentage24h = priceChangePercentage24h,
    )
}
