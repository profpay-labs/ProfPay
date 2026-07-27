package com.profpay.wallet.presentation.ui.feature.wallet.walletInfo
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.local.TokenLocal
import com.profpay.wallet.presentation.ui.components.feature.transaction.TransactionCardType
import com.profpay.wallet.presentation.ui.components.feature.transaction.TransactionHistoryList
import com.profpay.wallet.presentation.ui.shared.sharedPref
import com.profpay.wallet.presentation.viewmodel.dto.TokenName
import com.profpay.wallet.presentation.viewmodel.wallet.WalletInfoViewModel

@Composable
fun HorizontalPagerWalletInfoFeature(
    viewModel: WalletInfoViewModel,
    pagerState: PagerState,
    listTokensWithTotalBalance: List<TokenLocal?>,
    groupedTransaction: List<List<TransactionSummary?>>,
    goToWalletSots: () -> Unit,
    goToTXDetailsScreen: () -> Unit,
) {
    val sharedPref = sharedPref()

    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> {
                LazyColumn(
                    modifier =
                        Modifier
                            .padding(
                                vertical = 8.dp,
                                horizontal = 8.dp,
                            ).fillMaxSize(),
                ) {
                    itemsIndexed(listTokensWithTotalBalance) { _, tokenEntity ->
                        if (tokenEntity != null) {
                            val currentTokenName =
                                TokenName.entries
                                    .stream()
                                    .filter { it.tokenName == tokenEntity.tokenName }
                                    .findFirst()
                                    .orElse(TokenName.USDT)

                            CardForWalletInfoFeature(
                                onClick = {
                                    sharedPref.edit {
                                        putString(
                                            "token_name",
                                            tokenEntity.tokenName,
                                        )
                                    }
                                    goToWalletSots()
                                },
                                paintIconId = currentTokenName.paintIconId,
                                label = tokenEntity.tokenName,
                                shortNameToken = currentTokenName.shortName,
                                balance = tokenEntity.balance,
                                balanceForLastMonth = 32.0,
                                viewModel = viewModel,
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.size(10.dp)) }
                }
            }

            1 -> {
                TransactionHistoryList(
                    groupedTransaction = groupedTransaction,
                    type = TransactionCardType.INFO,
                    goToTXDetailsScreen = { goToTXDetailsScreen() },
                )
            }
        }
    }
}
