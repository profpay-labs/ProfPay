package com.profpay.data.contract.mapper

import com.profpay.data.contract.dto.*
import com.profpay.data.contract.dto.request.CallContractRequest
import com.profpay.data.contract.dto.request.CreateDealRequest
import com.profpay.data.contract.dto.request.DealStatusChangeRequestDto
import com.profpay.data.contract.dto.request.DeployContractRequest
import com.profpay.data.contract.dto.request.DisputeActionRequestDto
import com.profpay.data.contract.dto.response.CallContractResponseDto
import com.profpay.data.contract.dto.response.CreateDealResponseDto
import com.profpay.data.contract.dto.response.DealStatusChangeResponseDto
import com.profpay.data.contract.dto.response.DeployContractResponseDto
import com.profpay.data.contract.dto.response.DisputeActionResponseDto
import com.profpay.data.contract.dto.response.UserDealsResponseDto
import com.profpay.domain.contract.model.*
import com.profpay.domain.contract.model.params.CallContractParams
import com.profpay.domain.contract.model.params.CommissionCategoryParams
import com.profpay.domain.contract.model.params.CommissionParams
import com.profpay.domain.contract.model.params.ContractParams
import com.profpay.domain.contract.model.params.CreateDealParams
import com.profpay.domain.contract.model.params.DealStatusChangeParams
import com.profpay.domain.contract.model.params.DeployContractParams
import com.profpay.domain.contract.model.params.DisputeActionParams
import com.profpay.domain.contract.model.result.CallContractResult
import com.profpay.domain.contract.model.result.CreateDealResult
import com.profpay.domain.contract.model.result.DealStatusChangeResult
import com.profpay.domain.contract.model.result.DeployResult
import com.profpay.domain.contract.model.result.DisputeActionResult
import com.profpay.domain.contract.model.result.UserDealsResult

internal fun UserDealsResponseDto.toDomain(): UserDealsResult = UserDealsResult(
    deals = deals.map { it.toDomain() },
    timestampSeconds = timestamp,
)

internal fun DealDto.toDomain(): Deal = Deal(
    dealId = dealId,
    blockchainDealId = blockchainDealId,
    contractId = contractId,
    contractAddress = contractAddress,
    amount = amount,
    buyer = buyer.toDomain(),
    seller = seller.toDomain(),
    admins = admins.map { it.toDomain() },
    blockchainData = blockchainData?.toDomain(),
    disputeStatus = disputeStatus?.toDomain(),
)

internal fun DealParticipantDto.toDomain(): DealParticipant = DealParticipant(
    userId = userId,
    telegramId = telegramId,
    username = username,
    walletAddress = walletAddress,
    tier = tier.toDomain(),
)

internal fun UserTierDto.toDomain(): UserTier = UserTier(
    name = name,
    code = code,
    commissionPercent = commissionPercent,
)

internal fun DealBlockchainDataDto.toDomain(): DealBlockchainData = DealBlockchainData(
    seller = seller,
    buyer = buyer,
    amount = amount,
    score = score,
    ended = ended,
    totalExpertCommissions = totalExpertCommissions,
    paymentStatus = paymentStatus.toDomain(),
    agreementStatus = agreementStatus.toDomain(),
)

internal fun PaymentStatusDto.toDomain(): PaymentStatus = PaymentStatus(
    buyerDepositAndExpertFeePaid = buyerDepositAndExpertFeePaid,
    sellerExpertFeePaid = sellerExpertFeePaid,
)

internal fun AgreementStatusDto.toDomain(): AgreementStatus = AgreementStatus(
    sellerAgreed = sellerAgreed,
    buyerAgreed = buyerAgreed,
    disputed = disputed,
)

internal fun DisputeStatusDto.toDomain(): DisputeStatus = DisputeStatus(
    decisionAdmin = decisionAdmin,
    amountToSeller = amountToSeller,
    amountToBuyer = amountToBuyer,
    adminsAgreed = adminsAgreed,
    sellerAgreed = sellerAgreed,
    buyerAgreed = buyerAgreed,
    adminAgreedVoted = adminAgreedVoted,
    adminsDeclined = adminsDeclined,
    sellerDeclined = sellerDeclined,
    buyerDeclined = buyerDeclined,
    adminDeclinedVoted = adminDeclinedVoted,
)

internal fun DeployContractResponseDto.toDomain(): DeployResult = DeployResult(
    operationId = operationId,
    timestampSeconds = timestamp,
)

internal fun DeployContractParams.toDto(): DeployContractRequest = DeployContractRequest(
    userId = userId,
    appId = appId,
    ownerAddress = ownerAddress,
    contract = contract.toDto(),
    commission = commission.toDto(),
)

internal fun ContractParams.toDto(): ContractDeployData = ContractDeployData(
    address = address,
    contractName = contractName,
    amount = amount,
    estimateEnergy = estimateEnergy,
    bandwidthRequired = bandwidthRequired,
    txnBytes = txnBytes,
)

internal fun CommissionParams.toDto(): CommissionData = CommissionData(
    address = address,
    amount = amount,
    bandwidthRequired = bandwidthRequired,
    txnBytes = txnBytes,
)

internal fun CommissionCategoryParams.toDto(): CommissionCategory = CommissionCategory(
    type = type.name,
    amount = amount,
    description = description,
)

internal fun CreateDealResponseDto.toDomain(): CreateDealResult = CreateDealResult(
    dealId = dealId,
    contractAddress = contractAddress,
    arbiterAddresses = arbiterAddresses,
    timestampSeconds = timestamp,
)

internal fun CreateDealParams.toDto(): CreateDealRequest = CreateDealRequest(
    buyerUserId = buyerUserId,
    sellerUserId = sellerUserId,
    arbiterGroupId = arbiterGroupId,
    amount = amount,
)

internal fun CallContractResponseDto.toDomain(): CallContractResult = CallContractResult(
    operationId = operationId,
    timestampSeconds = timestamp,
)

internal fun CallContractParams.toDto(): CallContractRequest = CallContractRequest(
    userId = userId,
    appId = appId,
    ownerAddress = ownerAddress,
    changeStatus = changeStatus.name,
    contract = contract.toDto(),
    commission = commission.toDto(),
)

fun DisputeActionParams.toDto(): DisputeActionRequestDto = DisputeActionRequestDto(
    dealId = dealId,
    initiatorUserId = initiatorUserId,
    contractAddress = contractAddress,
    action = action.name,
)

fun DisputeActionResponseDto.toDomain(): DisputeActionResult = DisputeActionResult(
    dealId = dealId,
    action = DealChangeStatus.valueOf(action),
    participantsNotified = participantsNotified,
    timestamp = timestamp,
)

fun DealStatusChangeParams.toDto(): DealStatusChangeRequestDto = DealStatusChangeRequestDto(
    dealId = dealId,
    userId = userId,
    contractAddress = contractAddress,
    blockchainDealId = blockchainDealId,
    changeStatus = changeStatus.name,
)

fun DealStatusChangeResponseDto.toDomain(): DealStatusChangeResult = DealStatusChangeResult(
    dealId = dealId,
    newStatus = newStatus,
    buyerNotified = buyerNotified,
    sellerNotified = sellerNotified,
    timestamp = timestamp,
)
