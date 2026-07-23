package com.profpay.domain.contract.repository

import com.profpay.domain.contract.model.params.CallContractParams
import com.profpay.domain.contract.model.result.CallContractResult
import com.profpay.domain.contract.model.params.CreateDealParams
import com.profpay.domain.contract.model.result.CreateDealResult
import com.profpay.domain.contract.model.params.DealStatusChangeParams
import com.profpay.domain.contract.model.result.DealStatusChangeResult
import com.profpay.domain.contract.model.params.DeployContractParams
import com.profpay.domain.contract.model.result.DeployResult
import com.profpay.domain.contract.model.params.DisputeActionParams
import com.profpay.domain.contract.model.result.DisputeActionResult
import com.profpay.domain.contract.model.result.UserDealsResult

interface ContractRepository {

    /**
     * Получить сделки пользователя
     */
    suspend fun getUserDeals(userId: Long): Result<UserDealsResult>

    /**
     * Деплой смарт-контракта
     */
    suspend fun deployContract(params: DeployContractParams): Result<DeployResult>

    /**
     * Создать сделку между покупателем и продавцом
     */
    suspend fun createDeal(params: CreateDealParams): Result<CreateDealResult>

    /**
     * Вызов метода контракта (подтверждение, отмена, диспут и т.д.)
     */
    suspend fun callContract(params: CallContractParams): Result<CallContractResult>

    /**
     * Обработать действие по диспуту
     */
    suspend fun processDisputeAction(params: DisputeActionParams): Result<DisputeActionResult>

    /**
     * Обработать изменение статуса сделки
     */
    suspend fun processDealStatusChange(params: DealStatusChangeParams): Result<DealStatusChangeResult>
}
