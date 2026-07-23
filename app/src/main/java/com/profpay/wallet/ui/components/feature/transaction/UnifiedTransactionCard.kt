package com.profpay.wallet.ui.components.feature.transaction

import StackedSnakbarHostState
import androidx.compose.runtime.Composable
import androidx.core.content.edit
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.common.format.formatCurrency
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.bridge.viewmodel.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.ui.feature.wallet.transaction.CACardHistoryTransactionsFeature
import com.profpay.wallet.ui.feature.wallet.walletAddress.horizontalListsTrans.CardHistoryTransactionsForWAFeature
import com.profpay.wallet.ui.feature.wallet.walletInfo.CardHistoryTransactionsFeature
import com.profpay.wallet.ui.shared.sharedPref

@Composable
fun UnifiedTransactionCard(
    transaction: TransactionSummary,
    type: TransactionCardType,
    viewModel: WalletAddressViewModel? = null,
    addressWithTokens: AddressWithTokensLocal? = null,
    stackedSnackbarHostState: StackedSnakbarHostState? = null,
    goToSystemTRX: () -> Unit = {},
    goToTXDetailsScreen: () -> Unit = {},
) {
    val sharedPref = sharedPref()
    val addressWa = sharedPref.getString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, "")

    val currentTokenName =
        TokenName.entries
            .find { it.tokenName == transaction.tokenName } ?: TokenName.USDT

    val currentAddress =
        if (transaction.receiverAddress == addressWa) {
            transaction.senderAddress
        } else {
            transaction.receiverAddress
        }

    when (type) {
        TransactionCardType.INFO ->
            CardHistoryTransactionsFeature(
                onClick = {
                    sharedPref.edit { putLong("transaction_id", transaction.transactionId!!) }
                    goToTXDetailsScreen()
                },
                paintIconId = currentTokenName.paintIconId,
                shortNameToken = currentTokenName.shortName,
                transactionEntity = transaction,
                amount = transaction.amount.toTokenAmount().formatCurrency(),
                typeTransaction = transaction.type,
                address = currentAddress,
            )

        TransactionCardType.WA -> {
            if (viewModel == null || stackedSnackbarHostState == null || addressWithTokens == null) {
                return
            }
            CardHistoryTransactionsForWAFeature(
                viewModel = viewModel,
                onClick = {
                    sharedPref.edit { putLong("transaction_id", transaction.transactionId) }
                    goToTXDetailsScreen()
                },
                paintIconId = currentTokenName.paintIconId,
                shortNameToken = currentTokenName.shortName,
                amount = transaction.amount.toTokenAmount().formatCurrency(),
                typeTransaction = transaction.type,
                address = currentAddress,
                transactionEntity = transaction,
                stackedSnackbarHostState = stackedSnackbarHostState,
                goToSystemTRX = { goToSystemTRX() },
                addressWithTokens = addressWithTokens,
            )
        }

        TransactionCardType.CA ->
            CACardHistoryTransactionsFeature(
                paintIconId = currentTokenName.paintIconId,
                shortNameToken = currentTokenName.shortName,
                transactionEntity = transaction,
                amount = transaction.amount.toTokenAmount().formatCurrency(),
            )
    }
}
