package com.profpay.domain.market.repository

import com.profpay.domain.market.model.local.ExchangeRateLocal

/**
 * Локальный репозиторий курсов валют.
 */
interface ExchangeRatesLocalRepository {

    /**
     * Вставить новый курс.
     * @return ID вставленной записи
     */
    suspend fun insert(exchangeRate: ExchangeRateLocal): Long

    /**
     * Проверить, существует ли курс для символа.
     */
    suspend fun exists(symbol: String): Boolean

    /**
     * Обновить курс валюты.
     */
    suspend fun update(symbol: String, rate: Double)

    /**
     * Получить курс валюты.
     */
    suspend fun getRate(symbol: String): Double

    /**
     * Вставить или обновить курс (upsert).
     */
    suspend fun upsert(id: Long, symbol: String, rate: Double) {
        if (exists(symbol)) {
            update(symbol, rate)
        } else {
            insert(ExchangeRateLocal(id = id, symbol = symbol, rate = rate))
        }
    }
}
