package com.profpay.wallet.presentation.ui.screens.wallet

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.common.ext.toBigInteger
import com.profpay.core.common.format.formatCurrency
import com.profpay.core.database.entities.wallet.TransactionStatusCode
import com.profpay.core.database.entities.wallet.getTransactionStatusName
import com.profpay.wallet.presentation.ui.components.custom.CustomBottomCard
import com.profpay.wallet.presentation.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.presentation.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.presentation.ui.feature.wallet.txdetails.CardTextForTxDetailsFeature
import com.profpay.wallet.presentation.ui.feature.wallet.txdetails.aml.AmlAndButtonGetAmlForTXDetailsFeature
import com.profpay.wallet.presentation.ui.shared.sharedPref
import com.profpay.wallet.presentation.ui.shared.utils.convertTimestampToDateTime
import com.profpay.wallet.presentation.viewmodel.dto.TokenName
import com.profpay.wallet.presentation.viewmodel.wallet.TXDetailsViewModel
import com.profpay.wallet.presentation.viewmodel.wallet.aml.PdfDownloadUiEvent
import rememberStackedSnackbarHostState
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TXDetailsScreen(
    goToBack: () -> Unit,
    viewModel: TXDetailsViewModel = hiltViewModel(),
) {
    val amlState by viewModel.amlState.collectAsStateWithLifecycle()
    val pdfDownloadEvent by viewModel.pdfDownloadEvent.collectAsStateWithLifecycle()
    val rate by viewModel.trxUsdtRate.collectAsStateWithLifecycle()

    val sharedPref = sharedPref()

    val transactionId = sharedPref.getLong("transaction_id", 1)

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()

    val transactionEntity by viewModel.getTransactionLiveDataById(transactionId).observeAsState()

    val amlFeeResult by viewModel.amlFeeResult.collectAsStateWithLifecycle()
    val walletName by viewModel.walletName.collectAsStateWithLifecycle()

    val (isReceive, setIsReceive) = remember { mutableStateOf(false) }
    val (_, setIsProcessed) = remember { mutableStateOf(false) }
    val (amlReleaseDialog, setAmlReleaseDialog) = remember { mutableStateOf(false) }
    var dollarAmount by remember { mutableStateOf("0.0") }

    val context = LocalContext.current

    // Обработка скачивания PDF
    LaunchedEffect(pdfDownloadEvent) {
        when (val event = pdfDownloadEvent) {
            is PdfDownloadUiEvent.Success -> {
                val txId = transactionEntity?.txId ?: "aml_report"
                val uri = savePdfToDownloads(context, event.pdfBytes, txId)

                if (uri != null) {
                    stackedSnackbarHostState.showSuccessSnackbar(
                        "Успешное сохранение",
                        "Файл был успешно сохранен в папку 'Загрузки'",
                        "Открыть",
                        action = {
                            openPdfFile(context, uri)
                        },
                    )
                }
                viewModel.resetPdfDownloadEvent()
            }
            is PdfDownloadUiEvent.Error -> {
                stackedSnackbarHostState.showErrorSnackbar(
                    title = "Ошибка",
                    description = event.message,
                )
                viewModel.resetPdfDownloadEvent()
            }
            is PdfDownloadUiEvent.Loading -> {
                // Можно показать индикатор загрузки
            }
            PdfDownloadUiEvent.Idle -> Unit
        }
    }

    // Загрузка данных при изменении транзакции
    LaunchedEffect(transactionEntity?.txId) {
        val tx = transactionEntity ?: return@LaunchedEffect

        viewModel.checkAmlIsPending(tx.txId)

        if (tx.receiverAddressId != null) {
            setIsReceive(true)
            viewModel.loadAmlReport(
                address = tx.receiverAddress,
                txId = tx.txId,
                tokenName = tx.tokenName,
            )
        }

        dollarAmount = if (tx.tokenName == "USDT") {
            tx.amount.toTokenAmount().formatCurrency()
        } else {
            (tx.amount.toTokenAmount() * rate.toBigDecimal()).formatCurrency()
        }

        setIsProcessed(tx.isProcessed)
    }

    // Инициализация
    LaunchedEffect(Unit) {
        viewModel.loadExchangeRate()
        viewModel.getWalletNameById()
    }

    val currentTokenName = TokenName.entries.find { it.tokenName == transactionEntity?.tokenName }
        ?: TokenName.USDT

    CustomScaffoldWallet(stackedSnackbarHostState = stackedSnackbarHostState) { bottomPadding ->
        CustomTopAppBar(title = "TX Details", goToBack = { goToBack() })
        CustomBottomCard(
            modifier = Modifier
                .weight(0.8f)
                .shadow(7.dp, RoundedCornerShape(16.dp)),
            modifierColumn = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            bottomPadding = bottomPadding,
        ) {
            CardTextForTxDetailsFeature(
                title = "Кошелёк",
                contentText = walletName ?: "",
                stackedSnackbarHostState = stackedSnackbarHostState,
                isDropdownMenu = false,
            )
            CardTextForTxDetailsFeature(
                title = "Статус транзакции",
                contentText = getTransactionStatusName(
                    TransactionStatusCode.fromIndex(
                        transactionEntity?.statusCode?.code ?: 3,
                    ),
                ),
                stackedSnackbarHostState = stackedSnackbarHostState,
                isDropdownMenu = false,
            )
            CardTextForTxDetailsFeature(
                title = "Адрес отправителя",
                contentText = transactionEntity?.senderAddress,
                stackedSnackbarHostState = stackedSnackbarHostState,
            )
            CardTextForTxDetailsFeature(
                title = "Адрес получения",
                contentText = transactionEntity?.receiverAddress,
                stackedSnackbarHostState = stackedSnackbarHostState,
            )
            CardTextForTxDetailsFeature(
                title = "Хэш транзакции",
                contentText = transactionEntity?.txId,
                stackedSnackbarHostState = stackedSnackbarHostState,
                isHashTransaction = true,
            )

            Text(
                text = "Сумма",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 4.dp, top = 12.dp),
            )
            CardTextForTxDetailsFeature(
                title = "${
                    (transactionEntity?.amount ?: BigInteger.ONE).toTokenAmount().formatCurrency()
                } ${currentTokenName.shortName}",
                title2 = "~$dollarAmount$",
            )
            if (!isReceive) {
                CardTextForTxDetailsFeature(
                    title = "Комиссия",
                    title2 = "${transactionEntity?.commission?.toTokenAmount()} USDT",
                )
            }
            CardTextForTxDetailsFeature(
                title = "Дата",
                title2 = convertTimestampToDateTime(transactionEntity?.timestamp ?: 1),
            )

            if (isReceive && transactionEntity != null) {
                AmlAndButtonGetAmlForTXDetailsFeature(
                    amlState = amlState,
                    viewModel = viewModel,
                    transactionEntity = transactionEntity!!,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    amlReleaseDialog = amlReleaseDialog,
                    setAmlReleaseDialog = setAmlReleaseDialog,
                    amlFeeResultText = (
                        amlFeeResult?.toBigInteger()?.toTokenAmount() ?: 0
                        ).toString(),
                )
            }
        }
    }
}

/**
 * Сохраняет PDF файл в папку Downloads
 * @return Uri сохранённого файла или null при ошибке
 */
private fun savePdfToDownloads(
    context: Context,
    pdfBytes: ByteArray,
    txId: String,
): android.net.Uri? {
    val fileName = "aml_$txId.pdf"
    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

    return uri?.also {
        resolver.openOutputStream(it)?.use { outputStream ->
            outputStream.write(pdfBytes)
        }
    }
}

/**
 * Открывает PDF файл
 */
private fun openPdfFile(context: Context, uri: android.net.Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val downloadsIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(downloadsIntent)
    }
}
