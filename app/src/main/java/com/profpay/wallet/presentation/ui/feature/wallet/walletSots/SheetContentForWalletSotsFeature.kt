package com.profpay.wallet.presentation.ui.feature.wallet.walletSots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.presentation.ui.components.custom.getBottomPadding
import com.profpay.wallet.presentation.ui.shared.sharedPref
import com.profpay.wallet.presentation.ui.theme.HexagonColor1
import com.profpay.wallet.presentation.ui.theme.HexagonColor2
import com.profpay.wallet.presentation.ui.theme.HexagonColor3
import com.profpay.wallet.presentation.ui.theme.HexagonColor4
import com.profpay.wallet.presentation.ui.theme.HexagonColor5
import com.profpay.wallet.presentation.ui.theme.HexagonColor6
import com.profpay.wallet.presentation.ui.theme.HexagonColor7
import com.profpay.wallet.presentation.viewmodel.dto.TokenName
import com.profpay.wallet.presentation.viewmodel.wallet.walletSot.WalletSotViewModel


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SheetContentForWalletSotsFeature(
    addressList: List<AddressWithTokensLocal>,
    goToWalletAddress: () -> Unit,
    viewModel: WalletSotViewModel,
    goToWalletArchivalSots: () -> Unit,
) {
    val sharedPref = sharedPref()
    val bottomPadding = getBottomPadding()
    val tokenName = sharedPref.getString("token_name", TokenName.USDT.tokenName)

    val listColors = listOf(
        HexagonColor1,
        HexagonColor2,
        HexagonColor3,
        HexagonColor4,
        HexagonColor5,
        HexagonColor6,
        HexagonColor7
    )

    val sortedAddresses = remember(addressList) {
        addressList
            .filter { it.address.sotIndex >= 0 }
            .sortedBy { it.address.sotIndex }
    }

    val onAddressClick = { address: AddressWithTokensLocal ->
        sharedPref.edit {
            putString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, address.address.address)
        }
        goToWalletAddress()
    }

    val onReplaceAddressClick = { address: AddressWithTokensLocal ->
        viewModel.creationOfANewCell(address.address)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(sortedAddresses) { index, address ->
            AddressCardForWalletSotsFeature(
                index = index,
                color = listColors[index % listColors.size],
                addressWithTokens = address,
                tokenName = tokenName ?: TokenName.USDT.tokenName,
                onAddressClick = {
                    onAddressClick(address)
                },
                onReplaceAddressClick = {
                    onReplaceAddressClick(address)
                }
            )
        }
        item {
            ArchivalSotsCardFeature(
                text = "Архивные соты",
                onClick = goToWalletArchivalSots,
                bottomPadding = bottomPadding
            )
        }
    }
}
