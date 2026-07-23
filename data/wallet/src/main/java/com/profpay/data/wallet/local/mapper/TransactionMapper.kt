package com.profpay.data.wallet.local.mapper

import com.profpay.core.database.entities.wallet.TransactionEntity
import com.profpay.core.database.models.TransactionModel
import com.profpay.domain.wallet.model.Transaction
import com.profpay.domain.wallet.model.TransactionStatusCode
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.TransactionType

/**
 * Маппер между Room entities и domain models.
 */
object TransactionMapper {

    fun TransactionEntity.toDomain(): Transaction = Transaction(
        id = transactionId ?: 0L,
        txId = txId,
        senderAddressId = senderAddressId,
        receiverAddressId = receiverAddressId,
        senderAddress = senderAddress,
        receiverAddress = receiverAddress,
        walletId = walletId,
        tokenName = tokenName,
        amount = amount,
        timestamp = timestamp,
        status = status,
        isProcessed = isProcessed,
        type = TransactionType.fromCode(type),
        statusCode = TransactionStatusCode.fromCode(statusCode),
        commission = commission,
    )

    fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
        transactionId = if (id == 0L) null else id,
        txId = txId,
        senderAddressId = senderAddressId,
        receiverAddressId = receiverAddressId,
        senderAddress = senderAddress,
        receiverAddress = receiverAddress,
        walletId = walletId,
        tokenName = tokenName,
        amount = amount,
        timestamp = timestamp,
        status = status,
        isProcessed = isProcessed,
        type = type.code,
        statusCode = statusCode.code,
        commission = commission,
    )

    fun TransactionModel.toDomain(): TransactionSummary = TransactionSummary(
        walletId = walletId,
        transactionId = transactionId ?: 0L,
        txId = txId,
        senderAddress = senderAddress,
        receiverAddress = receiverAddress,
        tokenName = tokenName,
        amount = amount,
        timestamp = timestamp,
        transactionDate = transactionDate,
        type = TransactionType.fromCode(type),
        statusCode = TransactionStatusCode.fromCode(statusCode),
        isProcessed = isProcessed,
    )
}
