package com.profpay.wallet.presentation.ui.feature.wallet.walletAddress.horizontalListsTrans

import StackedSnakbarHostState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.TransactionType
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.presentation.ui.components.feature.transaction.TransactionCard
import com.profpay.wallet.presentation.ui.feature.wallet.txdetails.bottomSheet.bottomSheetTransOnGeneralReceipt
import com.profpay.wallet.presentation.ui.feature.wallet.walletAddress.model.toUiModel
import com.profpay.wallet.presentation.ui.shared.sharedPref
import com.profpay.wallet.presentation.viewmodel.wallet.walletSot.WalletAddressViewModel

@Composable
fun CardHistoryTransactionsForWAFeature(
    viewModel: WalletAddressViewModel,
    transactionEntity: TransactionSummary,
    addressWithTokens: AddressWithTokensLocal,
    address: String,
    typeTransaction: TransactionType,
    paintIconId: Int,
    amount: String,
    shortNameToken: String,
    onClick: () -> Unit = {},
    goToSystemTRX: () -> Unit = {},
    stackedSnackbarHostState: StackedSnakbarHostState,
) {
    val sharedPref = sharedPref()
    val addressWa = sharedPref.getString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, "")

    val isGeneralAddressReceive by viewModel.isGeneralAddress.collectAsStateWithLifecycle()
    val isAddressActivated by viewModel.isActivated.collectAsStateWithLifecycle()

    LaunchedEffect(transactionEntity.receiverAddress) {
        viewModel.checkIsGeneralAddress(address)
        viewModel.checkActivation(transactionEntity.receiverAddress)
    }

    val uiModel = remember(
        transactionEntity,
        isGeneralAddressReceive,
        address,
        addressWa,
    ) {
        transactionEntity.toUiModel(
            typeTransaction = typeTransaction,
            address = address,
            addressWa = addressWa ?: "",
            isGeneralAddressReceive = isGeneralAddressReceive ?: false,
        )
    }

    val (_, setIsOpenTransOnGeneralReceiptSheet) =
        bottomSheetTransOnGeneralReceipt(
            addressWithTokens = addressWithTokens,
            snackbar = stackedSnackbarHostState,
            tokenName = transactionEntity.tokenName,
            walletId = transactionEntity.walletId,
            balance = transactionEntity.amount,
        )

    TransactionCard(
        title = uiModel.title,
        details = uiModel.details,
        amount = "$amount $shortNameToken",
        iconRes = paintIconId,
        onClick = onClick,
        extraContent = {
            if (uiModel.showGeneralReceiveCard) {
                GeneralReceiveCardButtonFeature(
                    isActivated = isAddressActivated ?: false,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    goToSystemTRX = goToSystemTRX,
                    setIsOpenTransOnGeneralReceiptSheet = setIsOpenTransOnGeneralReceiptSheet,
                )
            }
        },
    )
}

@Composable
private fun GeneralReceiveCardButtonFeature(
    isActivated: Boolean,
    stackedSnackbarHostState: StackedSnakbarHostState,
    goToSystemTRX: () -> Unit,
    setIsOpenTransOnGeneralReceiptSheet: (Boolean) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth()
                .shadow(7.dp, RoundedCornerShape(7.dp))
                .clickable {
                    if (!isActivated) {
                        stackedSnackbarHostState.showErrorSnackbar(
                            title = "Перевод валюты невозможен",
                            description = "Для активации необходимо перейти в «Системный TRX»",
                            actionTitle = "Перейти",
                            action = { goToSystemTRX() },
                        )
                    } else {
                        setIsOpenTransOnGeneralReceiptSheet(true)
                    }
                },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Принять на Главный адрес",
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
