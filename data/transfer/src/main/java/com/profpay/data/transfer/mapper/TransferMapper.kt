package com.profpay.data.transfer.mapper

import com.profpay.data.transfer.dto.CreateTransferRequest
import com.profpay.data.transfer.dto.CreateTransferResponseDto
import com.profpay.data.transfer.dto.TransactionDataDto
import com.profpay.data.transfer.dto.CommissionCategoryDto
import com.profpay.data.transfer.dto.EstimateCommissionRequest
import com.profpay.data.transfer.dto.EstimateCommissionResponseDto
import com.profpay.data.transfer.dto.TransferCommissionDataDto
import com.profpay.domain.transfer.model.CreateTransferParams
import com.profpay.domain.transfer.model.TransactionData
import com.profpay.domain.transfer.model.CommissionBreakdown
import com.profpay.domain.transfer.model.CommissionCategoryType
import com.profpay.domain.transfer.model.EstimateCommissionParams
import com.profpay.domain.transfer.model.EstimateCommissionResult
import com.profpay.domain.transfer.model.TransferCommissionData
import com.profpay.domain.transfer.model.TransferResult

internal fun CreateTransferResponseDto.toDomain(): TransferResult = TransferResult(
    operationId = operationId,
    timestampSeconds = timestamp,
)

internal fun CreateTransferParams.toDto(): CreateTransferRequest = CreateTransferRequest(
    userId = userId,
    txId = txId,
    token = token.name,
    transactionData = transactionData.toDto(),
    commissionData = commissionData.toDto(),
)

internal fun TransactionData.toDto(): TransactionDataDto = TransactionDataDto(
    address = address,
    receiverAddress = receiverAddress,
    amount = amount,
    bandwidthRequired = bandwidthRequired,
    estimateEnergy = estimateEnergy,
    txnBytes = txnBytes,
)

internal fun TransferCommissionData.toDto(): TransferCommissionDataDto = TransferCommissionDataDto(
    address = address,
    amount = amount,
    bandwidthRequired = bandwidthRequired,
    categories = categories.map { it.toDto() },
    txnBytes = txnBytes,
)

internal fun CommissionBreakdown.toDto(): CommissionCategoryDto =
    CommissionCategoryDto(
        type = type.name,
        amount = amount,
        description = description,
    )

internal fun EstimateCommissionResponseDto.toDomain(): EstimateCommissionResult =
    EstimateCommissionResult(
        commission = commission,
        categories = categories.map { it.toDomain() },
        timestampSeconds = timestamp,
    )

internal fun CommissionCategoryDto.toDomain(): CommissionBreakdown = CommissionBreakdown(
    type = CommissionCategoryType.fromString(type),
    amount = amount,
    description = description,
)

internal fun EstimateCommissionParams.toDto(): EstimateCommissionRequest =
    EstimateCommissionRequest(
        userId = userId,
        address = address,
        energyRequired = energyRequired,
        bandwidthRequired = bandwidthRequired,
    )
