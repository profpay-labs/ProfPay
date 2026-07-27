package com.profpay.wallet.presentation.ui.feature.wallet.walletInfo

import androidx.compose.runtime.Composable
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.TransactionType
import com.profpay.wallet.presentation.ui.components.feature.transaction.TransactionCard
import com.profpay.wallet.presentation.ui.feature.wallet.walletAddress.model.toUiModel

@Composable
fun CardHistoryTransactionsFeature(
    address: String,
    typeTransaction: TransactionType,
    paintIconId: Int,
    amount: String,
    shortNameToken: String,
    transactionEntity: TransactionSummary,
    onClick: () -> Unit = {},
) {
    val uiModel =
        transactionEntity.toUiModel(
            typeTransaction = typeTransaction,
            address = address,
        )

    TransactionCard(
        title = uiModel.title,
        details = uiModel.details,
        amount = "$amount $shortNameToken",
        iconRes = paintIconId,
        onClick = onClick,
    )
}
