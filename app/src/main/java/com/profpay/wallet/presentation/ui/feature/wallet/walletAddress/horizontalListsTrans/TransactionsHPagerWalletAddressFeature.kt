package com.profpay.wallet.presentation.ui.feature.wallet.walletAddress.horizontalListsTrans

import StackedSnakbarHostState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.wallet.presentation.ui.components.feature.transaction.TransactionCardType
import com.profpay.wallet.presentation.ui.components.feature.transaction.TransactionHistoryList
import com.profpay.wallet.presentation.ui.feature.wallet.walletAddress.model.GroupedTransactions
import com.profpay.wallet.presentation.viewmodel.wallet.walletSot.WalletAddressViewModel

@Composable
fun TransactionsHPagerWalletAddressFeature(
    pagerState: PagerState,
    viewModel: WalletAddressViewModel,
    stackedSnackbarHostState: StackedSnakbarHostState,
    groupedTransactions: GroupedTransactions,
    goToTXDetailsScreen: () -> Unit,
    goToSystemTRX: () -> Unit,
    addressWithTokens: AddressWithTokensLocal?,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        when (page) {
            // Все транзакции
            0 ->
                TransactionHistoryList(
                    groupedTransaction = groupedTransactions.all,
                    type = TransactionCardType.WA,
                    viewModel = viewModel,
                    addressWithTokens = addressWithTokens,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    goToSystemTRX = { goToSystemTRX() },
                    goToTXDetailsScreen = { goToTXDetailsScreen() },
                )
            // Отправленные
            1 -> {
                TransactionHistoryList(
                    groupedTransaction = groupedTransactions.sender,
                    type = TransactionCardType.WA,
                    viewModel = viewModel,
                    addressWithTokens = addressWithTokens,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    goToSystemTRX = { goToSystemTRX() },
                    goToTXDetailsScreen = { goToTXDetailsScreen() },
                )
            }
            // Полученные
            2 -> {
                TransactionHistoryList(
                    groupedTransaction = groupedTransactions.receiver,
                    type = TransactionCardType.WA,
                    viewModel = viewModel,
                    addressWithTokens = addressWithTokens,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    goToSystemTRX = { goToSystemTRX() },
                    goToTXDetailsScreen = { goToTXDetailsScreen() },
                )
            }
        }
    }
}
