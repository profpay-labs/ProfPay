package com.profpay.wallet.ui.feature.wallet.walletInfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.common.format.formatCurrency
import com.profpay.domain.market.model.CoinSymbol
import com.profpay.wallet.bridge.viewmodel.wallet.WalletInfoViewModel
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.RedColor
import java.math.BigInteger

@Composable
fun CardForWalletInfoFeature(
    paintIconId: Int,
    label: String,
    balance: BigInteger,
    balanceForLastMonth: Double = 0.0,
    shortNameToken: String,
    viewModel: WalletInfoViewModel,
    onClick: () -> Unit = {},
) {
    val rateValue by viewModel.trxUsdtRate.collectAsStateWithLifecycle()

    val (priceChangePercentage24hUsdt, setPriceChangePercentage24hUsdt) =
        remember {
            mutableDoubleStateOf(0.0)
        }
    val (priceChangePercentage24hTrx, setPriceChangePercentage24hTrx) =
        remember {
            mutableDoubleStateOf(0.0)
        }

    LaunchedEffect(Unit) {
        viewModel.loadTrxUsdtRate()
        setPriceChangePercentage24hUsdt(
            viewModel.tradingInsightsLocalRepository.getPriceChange24h(CoinSymbol.USDT_TRC20.id),
        )
        setPriceChangePercentage24hTrx(
            viewModel.tradingInsightsLocalRepository.getPriceChange24h(CoinSymbol.TRON.id),
        )
    }

    val priceInUsdt: String =
        if (label == "TRX") {
            (balance.toTokenAmount() * rateValue).formatCurrency()
        } else {
            balance.toTokenAmount().formatCurrency()
        }

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .shadow(7.dp, RoundedCornerShape(10.dp)),
        onClick = { onClick() },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(start = 10.dp, end = 16.dp)
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .size(40.dp)
                                .paint(
                                    painterResource(id = paintIconId),
                                    contentScale = ContentScale.FillBounds,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {}
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "${balance.toTokenAmount().formatCurrency()} $shortNameToken",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
            ) {
                Text(text = "$$priceInUsdt", style = MaterialTheme.typography.bodySmall)

                val priceChangePercentage24h =
                    if (shortNameToken == "TRX") {
                        priceChangePercentage24hTrx
                    } else {
                        priceChangePercentage24hUsdt
                    }

                if (priceChangePercentage24h >= 0.0) {
                    Text(
                        "+${priceChangePercentage24h.toBigDecimal().formatCurrency()}%",
                        color = GreenColor,
                        style = MaterialTheme.typography.labelLarge,
                    )
                } else {
                    Text(
                        "${priceChangePercentage24h.toBigDecimal().formatCurrency()}%",
                        color = RedColor,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
