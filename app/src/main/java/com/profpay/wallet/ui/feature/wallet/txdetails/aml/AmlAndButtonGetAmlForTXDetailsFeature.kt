
package com.profpay.wallet.ui.feature.wallet.txdetails.aml

import StackedSnakbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.domain.aml.model.AmlStatus
import com.profpay.domain.wallet.model.Transaction
import com.profpay.wallet.bridge.viewmodel.wallet.TXDetailsViewModel
import com.profpay.wallet.bridge.viewmodel.wallet.aml.AmlPaymentUiEvent
import com.profpay.wallet.bridge.viewmodel.wallet.aml.AmlUiState
import com.profpay.wallet.ui.app.theme.backgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.greenColor
import com.profpay.wallet.ui.screens.wallet.aml.toAmlType
import com.profpay.wallet.ui.widgets.dialog.AlertDialogWidget

@Composable
fun AmlAndButtonGetAmlForTXDetailsFeature(
    amlState: AmlUiState,
    viewModel: TXDetailsViewModel,
    transactionEntity: Transaction,
    stackedSnackbarHostState: StackedSnakbarHostState,
    amlReleaseDialog: Boolean,
    setAmlReleaseDialog: (Boolean) -> Unit,
    amlFeeResultText: String,
) {
    val amlPaymentEvent by viewModel.amlPaymentEvent.collectAsStateWithLifecycle()
    val (amlButtonIsEnabled, setAmlButtonIsEnabled) = remember { mutableStateOf(true) }

    // Обработка событий платежа AML
    LaunchedEffect(amlPaymentEvent) {
        when (val event = amlPaymentEvent) {
            is AmlPaymentUiEvent.Success -> {
                setAmlButtonIsEnabled(false) // Оставляем кнопку неактивной после успеха
                stackedSnackbarHostState.showSuccessSnackbar(
                    "Успешное действие",
                    event.message,
                    "Закрыть",
                )
                viewModel.resetAmlPaymentEvent()
            }
            is AmlPaymentUiEvent.Error -> {
                setAmlButtonIsEnabled(true)
                stackedSnackbarHostState.showErrorSnackbar(
                    event.title,
                    event.message,
                    "Закрыть",
                )
                viewModel.resetAmlPaymentEvent()
            }
            is AmlPaymentUiEvent.Loading -> {
                setAmlButtonIsEnabled(false)
            }
            AmlPaymentUiEvent.Idle -> Unit
        }
    }

    // Отображение состояния AML отчёта
    when (amlState) {
        is AmlUiState.Success -> {
            val report = amlState.report

            if (report.status == AmlStatus.PENDING) {
                LaunchedEffect(Unit) {
                    stackedSnackbarHostState.showInfoSnackbar(
                        "AML",
                        "Ваш AML находится в обработке, ожидайте.",
                        "Закрыть",
                    )
                }
            }

            if (report.amlId.isNotEmpty()) {
                KnowAMLFeature(
                    viewModel = viewModel,
                    amlType = report.toAmlType(),
                    amlState = report,
                    transactionEntity = transactionEntity,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                )
            } else {
                UnknownAMLFeature()
            }
        }

        is AmlUiState.Error -> {
            LaunchedEffect(amlState.message) {
                stackedSnackbarHostState.showErrorSnackbar(
                    "Ошибка запроса",
                    amlState.message,
                    "Закрыть",
                )
            }
            UnknownAMLFeature()
        }

        is AmlUiState.Loading -> {
            // Можно добавить индикатор загрузки
            UnknownAMLFeature()
        }

        AmlUiState.Idle -> {
            UnknownAMLFeature()
        }
    }

    // Кнопка "Получить AML"
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(bottom = 10.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Button(
            onClick = {
                setAmlReleaseDialog(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.greenColor,
                contentColor = MaterialTheme.colorScheme.backgroundContainerButtonLight,
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = amlButtonIsEnabled,
        ) {
            Text(
                text = "Получить AML",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

    // Диалог подтверждения оплаты
    if (amlReleaseDialog) {
        AlertDialogWidget(
            onConfirmation = {
                setAmlReleaseDialog(false)
                viewModel.processAmlPayment(
                    address = transactionEntity.receiverAddress,
                    txId = transactionEntity.txId,
                )
            },
            onDismissRequest = {
                setAmlReleaseDialog(false)
            },
            dialogTitle = "Выпуск AML",
            dialogText = """
                Для получения AML необходимо внести плату за его выпуск или перевыпуск в размере $amlFeeResultText TRX.

                Это обязательная процедура, которая обеспечивает актуализацию и соответствие AML требованиям текущего законодательства и стандартов.

                Сумма будет списана с центрального адреса которому принадлежит данный адрес!
            """.trimIndent(),
            textConfirmButton = "Оплатить и получить",
            textDismissButton = "Закрыть",
        )
    }
}
