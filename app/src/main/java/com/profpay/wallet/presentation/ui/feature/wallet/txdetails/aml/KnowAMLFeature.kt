package com.profpay.wallet.presentation.ui.feature.wallet.txdetails.aml

import StackedSnakbarHostState
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profpay.core.common.format.RelativeDateFormatter.formatRelative
import com.profpay.domain.aml.model.AmlReport
import com.profpay.domain.aml.model.RiskLevel
import com.profpay.domain.wallet.model.Transaction
import com.profpay.wallet.R
import com.profpay.wallet.presentation.ui.screens.wallet.AMLType
import com.profpay.wallet.presentation.ui.theme.RedColor
import com.profpay.wallet.presentation.viewmodel.wallet.TXDetailsViewModel

@SuppressLint("DefaultLocale")
@Composable
fun KnowAMLFeature(
    viewModel: TXDetailsViewModel,
    amlType: AMLType,
    amlState: AmlReport,
    transactionEntity: Transaction,
    stackedSnackbarHostState: StackedSnakbarHostState,
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier =
                Modifier
                    .padding(bottom = 22.dp)
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = amlType.label,
                color = amlType.color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.icon_storm),
                    contentDescription = "",
                    tint = amlType.color,
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "${String.format("%.2f", amlState.riskScore)}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .padding(bottom = 22.dp)
                    .fillMaxWidth(),
        ) {
            Text(
                text = amlType.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Card(
            shape = RoundedCornerShape(10.dp),
            modifier =
                Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(3.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 8.dp, bottom = 40.dp),
            ) {
                if (amlState.signals.isNotEmpty()) {
                    Text(
                        text = "Расшифровка уровня рисков",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(),
                    )

                    for (signal in amlState.signals) {
                        if (signal.riskLevel == RiskLevel.HIGH) {
                            Row(
                                modifier = Modifier.padding(top = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(20.dp)
                                            .paint(
                                                painterResource(id = R.drawable.icon_stop),
                                                contentScale = ContentScale.FillBounds,
                                            ),
                                )
                                Text(
                                    text = "Danger",
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(start = 12.dp),
                                    fontSize = 18.sp,
                                )
                            }

                            Row(
                                modifier =
                                    Modifier
                                        .padding(top = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(0.3f),
                                    text = "${String.format("%.1f", signal.percentage)}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RedColor,
                                )
                                Text(
                                    text = signal.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    for (signal in amlState.signals) {
                        if (signal.riskLevel == RiskLevel.MEDIUM) {
                            Row(
                                modifier = Modifier.padding(top = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(20.dp)
                                            .paint(
                                                painterResource(id = R.drawable.icon_warning),
                                                contentScale = ContentScale.FillBounds,
                                            ),
                                )
                                Text(
                                    text = "Suspicious sources",
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(start = 12.dp),
                                    fontSize = 18.sp,
                                )
                            }

                            Row(
                                modifier =
                                    Modifier
                                        .padding(top = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(0.3f),
                                    text = "${String.format("%.1f", signal.percentage)}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB218),
                                )
                                Text(
                                    text = signal.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    for (signal in amlState.signals) {
                        if (signal.riskLevel == RiskLevel.LOW) {
                            Row(
                                modifier = Modifier.padding(top = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(20.dp)
                                            .paint(
                                                painterResource(id = R.drawable.icon_check_mark),
                                                contentScale = ContentScale.FillBounds,
                                            ),
                                )
                                Text(
                                    text = "Trusted sources",
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(start = 12.dp),
                                    fontSize = 18.sp,
                                )
                            }

                            Row(
                                modifier =
                                    Modifier
                                        .padding(top = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(0.3f),
                                    text = "${String.format("%.1f", signal.percentage)}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF51B413),
                                )
                                Text(
                                    text = signal.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    Row(
                        modifier =
                            Modifier
                                .padding(top = 40.dp)
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            onClick = {
                                viewModel.downloadPdf(txId = transactionEntity.txId)
                            },
                            modifier =
                                Modifier
                                    .padding(),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = amlType.color.copy(alpha = 0.2f),
                                    contentColor = amlType.color,
                                ),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Text(
                                text = "Скачать PDF отчёт",
//                            fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(80.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.icon_search_to_file),
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )

                        Text(
                            text = "Расшифровка отсутствует",
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = 18.sp,
                        )
                    }
                }
            }
        }

        Text(
            text = "Проверенно: ${formatRelative(amlState.createdAtSeconds)}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.surfaceBright,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}
