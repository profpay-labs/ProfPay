package com.profpay.wallet.ui.feature.wallet.walletSystem

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profpay.domain.wallet.model.local.WalletProfileSummary

@Composable
fun LazyListWalletSystemFeature(
    walletList: List<WalletProfileSummary>,
    currentWalletId: Long,
    onWalletSelected: (walletId: Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(
            items = walletList,
            key = { wallet -> wallet.id ?: 0L }, // Stable keys для оптимизации
        ) { wallet ->
            CardForWalletSystemFeature(
                wallet = wallet,
                onClick = {
                    wallet.id?.let { onWalletSelected(it) }
                },
                selected = wallet.id == currentWalletId,
            )
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
