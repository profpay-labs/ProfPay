package com.profpay.data.contract.repository

import com.profpay.core.network.error.NetworkError
import com.profpay.core.network.error.safeApiCall
import com.profpay.data.contract.api.ContractApi
import com.profpay.data.contract.dto.response.CallContractResponseDto
import com.profpay.data.contract.dto.response.CreateDealResponseDto
import com.profpay.data.contract.dto.response.DealStatusChangeResponseDto
import com.profpay.data.contract.dto.response.DeployContractResponseDto
import com.profpay.data.contract.dto.response.DisputeActionResponseDto
import com.profpay.data.contract.dto.response.UserDealsResponseDto
import com.profpay.data.contract.mapper.toDomain
import com.profpay.data.contract.mapper.toDto
import com.profpay.domain.contract.exception.ContractError
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
import com.profpay.domain.contract.repository.ContractRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.map

@Singleton
internal class ContractRepositoryImpl @Inject constructor(
    private val contractApi: ContractApi,
    private val json: Json,
) : ContractRepository {

    override suspend fun getUserDeals(userId: Long): Result<UserDealsResult> {
        val apiResult: Result<UserDealsResponseDto> = safeApiCall(json) {
            contractApi.getUserDeals(userId)
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toContractError(userId) }
    }

    override suspend fun deployContract(params: DeployContractParams): Result<DeployResult> {
        val apiResult: Result<DeployContractResponseDto> = safeApiCall(json) {
            contractApi.deployContract(params.toDto())
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toDeployError() }
    }

    override suspend fun createDeal(params: CreateDealParams): Result<CreateDealResult> {
        val apiResult: Result<CreateDealResponseDto> = safeApiCall(json) {
            contractApi.createDeal(params.toDto())
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toCreateDealError(params) }
    }

    override suspend fun callContract(params: CallContractParams): Result<CallContractResult> {
        val apiResult: Result<CallContractResponseDto> = safeApiCall(json) {
            contractApi.callContract(params.toDto())
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toCallContractError(params.ownerAddress) }
    }

    override suspend fun processDisputeAction(
        params: DisputeActionParams,
    ): Result<DisputeActionResult> {
        val apiResult: Result<DisputeActionResponseDto> = safeApiCall(json) {
            contractApi.processDisputeAction(params.toDto())
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toDisputeActionError(params) }
    }

    override suspend fun processDealStatusChange(
        params: DealStatusChangeParams,
    ): Result<DealStatusChangeResult> {
        val apiResult: Result<DealStatusChangeResponseDto> = safeApiCall(json) {
            contractApi.processDealStatusChange(params.toDto())
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toDealStatusChangeError(params) }
    }

    private fun Throwable.toContractError(userId: Long): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isNotFound -> ContractError.UserNotFound(userId)
            isServerError -> ContractError.ContractUnavailable(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toDeployError(): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            code == 400 -> ContractError.InvalidDeployRequest(errorBody ?: "Bad request")
            isServerError -> ContractError.ContractUnavailable(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toCreateDealError(params: CreateDealParams): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isNotFound -> ContractError.BuyerContractNotFound(params.buyerUserId)
            code == 422 -> ContractError.InsufficientArbiters(params.arbiterGroupId)
            isServerError -> ContractError.ContractUnavailable(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toCallContractError(contractAddress: String): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isNotFound -> ContractError.ContractNotFound(contractAddress)
            code == 400 -> ContractError.InvalidCallRequest(errorBody ?: "Bad request")
            isServerError -> ContractError.ContractUnavailable(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toDisputeActionError(params: DisputeActionParams): Throwable =
        when (this) {
            is NetworkError.HttpError -> when {
                isNotFound -> ContractError.DealNotFound(params.dealId)
                isBadRequest -> ContractError.InvalidDisputeAction(
                    action = params.action.name,
                )
                isForbidden -> ContractError.NotDisputeParticipant(
                    userId = params.initiatorUserId,
                    dealId = params.dealId,
                )
                isServerError -> ContractError.ContractUnavailable(this)
                else -> this
            }
            else -> this
        }

    private fun Throwable.toDealStatusChangeError(params: DealStatusChangeParams): Throwable =
        when (this) {
            is NetworkError.HttpError -> when {
                isNotFound -> ContractError.DealNotFound(params.dealId)
                isBadRequest -> ContractError.InvalidCallRequest(
                    reason = "Invalid status change: ${params.changeStatus}",
                )
                isServerError -> ContractError.ContractUnavailable(this)
                else -> this
            }
            else -> this
        }
}
