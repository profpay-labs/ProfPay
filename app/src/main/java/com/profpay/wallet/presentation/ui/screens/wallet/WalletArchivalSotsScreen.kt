package com.profpay.wallet.presentation.ui.screens.wallet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.presentation.ui.components.custom.CustomBottomCard
import com.profpay.wallet.presentation.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.presentation.ui.components.custom.CustomTabRow
import com.profpay.wallet.presentation.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.presentation.ui.feature.wallet.walletArchivalSots.ArchivalAddressListFeature
import com.profpay.wallet.presentation.ui.feature.wallet.walletArchivalSots.EmptyArchivalListFeature
import com.profpay.wallet.presentation.ui.feature.wallet.walletArchivalSots.TopCardForArchivalAddressFeature
import com.profpay.wallet.presentation.ui.shared.sharedPref
import com.profpay.wallet.presentation.viewmodel.dto.TokenName
import com.profpay.wallet.presentation.viewmodel.wallet.walletSot.WalletArchivalSotViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WalletArchivalSotsScreen(
    goToBack: () -> Unit,
    goToWalletAddress: () -> Unit,
    viewModel: WalletArchivalSotViewModel = hiltViewModel(),
) {
    val sharedPref = sharedPref()

    val token =
        sharedPref.getString("token_name", TokenName.USDT.tokenName) ?: TokenName.USDT.tokenName

    val addressWithTokensArchival by viewModel
        .getAddressWithTokensArchivalByBlockchain(
            blockchainName = TokenName.valueOf(token).blockchainName,
        ).observeAsState(emptyList())

    val addressWTAWithFunds =
        remember(addressWithTokensArchival) {
            viewModel.getAddressesWTAWithFunds(
                addressWithTokensArchival,
                token,
            )
        }
    CustomScaffoldWallet { bottomPadding ->
        CustomTopAppBar(title = "Wallet Archival Sots", goToBack = { goToBack() })
        TopCardForArchivalAddressFeature(
            title = "Архив сот",
            text = "Список ваших замененных адресов сот.",
        )
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn =
                Modifier
                    .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            bottomPadding = bottomPadding,
        ) {
            val titles = listOf("All", "With funds")
            val pagerState = rememberPagerState(pageCount = { titles.size })
            CustomTabRow(titles = titles, pagerState = pagerState)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val list =
                    when (page) {
                        0 -> addressWithTokensArchival
                        else -> addressWTAWithFunds
                    }

                if (list.isNotEmpty()) {
                    ArchivalAddressListFeature(
                        addresses = list,
                        goToWalletAddress = goToWalletAddress,
                    )
                } else {
                    val emptyMessage =
                        if (page == 0) {
                            "У вас пока нет архивных сот..."
                        } else {
                            "Нет архивных сот\nс средствами..."
                        }
                    EmptyArchivalListFeature(emptyMessage)
                }
            }
        }
    }
}
