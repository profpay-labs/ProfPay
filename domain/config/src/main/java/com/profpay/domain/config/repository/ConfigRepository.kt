package com.profpay.domain.config.repository

import com.profpay.domain.config.model.FeeConfiguration

interface ConfigRepository {

    /**
     * Получить актуальную конфигурацию комиссий
     */
    suspend fun getFeeConfiguration(): Result<FeeConfiguration>
}
