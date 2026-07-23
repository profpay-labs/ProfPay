package com.profpay.wallet.ui.feature.wallet.send.bottomsheet

import StackedSnakbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.bridge.viewmodel.dto.transfer.TransferUiEvent
import com.profpay.wallet.bridge.viewmodel.wallet.send.SendFromWalletViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetTransferConfirmation(
    viewModel: SendFromWalletViewModel = hiltViewModel(),
    modelTransferFromBS: ModelTransferFromBS,
    snackbar: StackedSnakbarHostState,
): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { false },
        )
    val coroutineScope = rememberCoroutineScope()
    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }

    val (isProcessing, setIsProcessing) = remember { mutableStateOf(false) }
    val (isDetailsTransaction, setIsDetailsTransaction) = remember { mutableStateOf(false) }
    val (hasHandledSuccess, setHasHandledSuccess) = remember { mutableStateOf(false) }

    val transferEvent by viewModel.transferEvent.collectAsStateWithLifecycle()

    LaunchedEffect(transferEvent) {
        when (transferEvent) {
            is TransferUiEvent.Idle -> {
                // Не сбрасываем isProcessing здесь - это делается в Success/Error
            }
            is TransferUiEvent.Loading -> {
                setIsProcessing(true)
            }
            is TransferUiEvent.Success -> {
                if (hasHandledSuccess) return@LaunchedEffect
                setHasHandledSuccess(true)

                setIsProcessing(false)
                // Сначала закрываем модалку, потом consume
                coroutineScope.launch {
                    delay(300.milliseconds) // Небольшая задержка для UX
                    sheetState.hide()
                    delay(300.milliseconds)
                    setIsOpenSheet(false)
                    viewModel.consumeTransferEvent()
                    setHasHandledSuccess(false)
                }
            }
            is TransferUiEvent.Error -> {
                val e = transferEvent as TransferUiEvent.Error
                setIsProcessing(false)
                snackbar.showErrorSnackbar(
                    title = e.title,
                    description = e.message,
                    actionTitle = "Закрыть",
                )

                coroutineScope.launch {
                    sheetState.hide()
                    delay(300.milliseconds)
                    setIsOpenSheet(false)
                    viewModel.consumeTransferEvent() // ← Перенесли в конец
                }
            }
        }
    }

    if (isOpenSheet) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.height(IntrinsicSize.Min),
            onDismissRequest = {
                if (!isProcessing) {
                    coroutineScope.launch {
                        sheetState.hide()
                        delay(400.milliseconds)
                        setIsOpenSheet(false)
                    }
                }
            },
            sheetState = sheetState,
        ) {
            Column {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "Перевод", fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        if (!isProcessing) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    delay(400.milliseconds)
                                    setIsOpenSheet(false)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "",
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }

                if (isProcessing) {
                    ContentBottomSheetTransferProcessing(onClick = {
                        setIsDetailsTransaction(true)
                    })
                } else {
                    ContentBottomSheetTransferConfirmation(
                        viewModel = viewModel,
                        isDetails = isDetailsTransaction,
                        modelTransferFromBS = modelTransferFromBS,
                        closeBS = {
                            coroutineScope.launch {
                                sheetState.hide()
                                delay(400.milliseconds)
                                setIsOpenSheet(false)
                            }
                        }
                    )
                }
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}
