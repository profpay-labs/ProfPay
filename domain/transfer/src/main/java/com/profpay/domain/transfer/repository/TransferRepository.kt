package com.profpay.domain.transfer.repository

import com.profpay.domain.transfer.model.CreateTransferParams
import com.profpay.domain.transfer.model.EstimateCommissionParams
import com.profpay.domain.transfer.model.EstimateCommissionResult
import com.profpay.domain.transfer.model.TransferResult

interface TransferRepository {

    /**
     * Создать перевод криптовалюты
     */
    suspend fun createTransfer(params: CreateTransferParams): Result<TransferResult>

    /**
     * Оценить комиссию для транзакции
     */
    suspend fun estimateCommission(params: EstimateCommissionParams): Result<EstimateCommissionResult>
}
