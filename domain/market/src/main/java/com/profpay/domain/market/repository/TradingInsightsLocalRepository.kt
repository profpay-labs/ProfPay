package com.profpay.domain.market.repository

import com.profpay.domain.market.model.local.TradingInsightsLocal

/**
 * Локальный репозиторий торговой аналитики.
 */
interface TradingInsightsLocalRepository {

    /**
     * Вставить новую запись.
     * @return ID вставленной записи
     */
    suspend fun insert(tradingInsights: TradingInsightsLocal): Long

    /**
     * Проверить, существует ли запись для символа.
     */
    suspend fun exists(symbol: String): Boolean

    /**
     * Обновить процент изменения цены за 24 часа.
     */
    suspend fun updatePriceChange24h(symbol: String, percentage: Double)

    /**
     * Получить процент изменения цены за 24 часа.
     */
    suspend fun getPriceChange24h(symbol: String): Double

    /**
     * Вставить или обновить (upsert).
     */
    suspend fun upsert(symbol: String, percentage: Double) {
        if (exists(symbol)) {
            updatePriceChange24h(symbol, percentage)
        } else {
            insert(TradingInsightsLocal(symbol = symbol, priceChangePercentage24h = percentage))
        }
    }
}
