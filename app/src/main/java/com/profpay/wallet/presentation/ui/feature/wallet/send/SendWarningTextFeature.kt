package com.profpay.wallet.presentation.ui.feature.wallet.send

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.profpay.wallet.presentation.ui.theme.redColor

@Composable
fun SendWarningTextFeature(uiStateWarning: String?) {
    if (uiStateWarning != null) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    modifier = Modifier,
                    text = uiStateWarning,
                    color = MaterialTheme.colorScheme.redColor,
                )
            }
        }
    }
}
