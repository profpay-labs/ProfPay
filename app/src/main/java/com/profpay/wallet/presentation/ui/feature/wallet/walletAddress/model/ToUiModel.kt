package com.profpay.wallet.presentation.ui.feature.wallet.walletAddress.model

import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.TransactionType


fun TransactionSummary.toUiModel(
    typeTransaction: TransactionType,
    address: String,
    addressWa: String = "",
    isGeneralAddressReceive: Boolean = false,
): CardTransactionUiModel {
    val title =
        when (typeTransaction) {
            TransactionType.SEND -> "Отправлено"
            TransactionType.RECEIVE -> "Получено"
            TransactionType.BETWEEN_YOURSELF -> "Между своими"
            else -> ""
        }

    val details =
        when (typeTransaction) {
            TransactionType.SEND -> "Куда: ${address.take(5)}...${address.takeLast(5)}"
            TransactionType.RECEIVE -> "Откуда: ${address.take(5)}...${address.takeLast(5)}"
            TransactionType.BETWEEN_YOURSELF ->
                "Откуда: ${senderAddress.take(5)}...${senderAddress.takeLast(5)}\n" +
                    "Куда: ${receiverAddress.take(5)}...${receiverAddress.takeLast(5)}"
            else -> ""
        }

    val betweenYourselfReceiver =
        typeTransaction == TransactionType.BETWEEN_YOURSELF && receiverAddress == addressWa
    val showGeneralReceiveCard =
        (!isGeneralAddressReceive && typeTransaction == TransactionType.RECEIVE && !isProcessed) ||
            (!isGeneralAddressReceive && betweenYourselfReceiver && !isProcessed)

    return CardTransactionUiModel(
        title = title,
        details = details,
        showGeneralReceiveCard = showGeneralReceiveCard,
    )
}
