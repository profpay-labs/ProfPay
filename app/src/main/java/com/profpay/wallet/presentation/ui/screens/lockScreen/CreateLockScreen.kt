package com.profpay.wallet.presentation.ui.screens.lockScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.R
import com.profpay.wallet.presentation.ui.feature.lockScreen.InputDots
import com.profpay.wallet.presentation.ui.feature.lockScreen.NumberBoard
import com.profpay.wallet.presentation.viewmodel.pinlock.PinLockViewModel
import com.profpay.wallet.presentation.viewmodel.pinlock.PinUiState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CreateLockScreen(
    toNavigate: () -> Unit,
    viewModel: PinLockViewModel = hiltViewModel(),
    goToBack: () -> Unit = {},
    goingBack: Boolean = false,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val inputPinCode = remember { mutableStateListOf<Int>() }
    val repeatInputPinCode = remember { mutableStateListOf<Int>() }
    var isError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isRepeatPhase = inputPinCode.size == 4

    // Обработка состояний от ViewModel
    LaunchedEffect(uiState) {
        when (uiState) {
            PinUiState.Success -> {
                toNavigate()
            }
            is PinUiState.Error -> {
                val errorMessage = (uiState as PinUiState.Error).message
                snackbarHostState.showSnackbar(errorMessage)

                // Сбрасываем всё при ошибке сохранения
                inputPinCode.clear()
                repeatInputPinCode.clear()
                viewModel.resetState()
            }
            else -> { /* Idle, Loading, ValidationFailed */ }
        }
    }

    // Проверка совпадения PIN-кодов и сохранение
    LaunchedEffect(inputPinCode.size, repeatInputPinCode.size) {
        if (inputPinCode.size == 4 && repeatInputPinCode.size == 4) {
            delay(250.milliseconds)

            val firstPin = inputPinCode.joinToString(separator = "")
            val repeatPin = repeatInputPinCode.joinToString(separator = "")

            if (firstPin == repeatPin) {
                viewModel.saveNewPin(repeatPin)
            } else {
                isError = true
                repeatInputPinCode.clear()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFFff2a00),
                        contentColor = Color.White,
                    )
                },
            )
        },
    ) { padding ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.fillMaxWidth(0.1f))

                Icon(
                    painter = painterResource(id = R.drawable.icon_smart),
                    contentDescription = "",
                    modifier =
                        Modifier
                            .fillMaxSize(0.5f)
                            .clip(CircleShape)
                            .weight(1f),
                )
                Spacer(modifier = Modifier.fillMaxHeight(0.02f))

                Text(text = "Hii, User!")

                if (inputPinCode.size < 4) {
                    Text(text = "Create 4-digit security PIN")
                } else {
                    Text(text = "Repeat 4-digit security PIN")
                }

                Spacer(modifier = Modifier.fillMaxHeight(0.02f))

                // Показываем нужные dots в зависимости от фазы
                if (!isRepeatPhase) {
                    InputDots(numbers = inputPinCode)
                } else {
                    InputDots(
                        numbers = repeatInputPinCode,
                        isError = isError,
                        onErrorReset = { isError = false },
                    )
                }

                NumberBoard(
                    inputPinCode = inputPinCode,
                    isCreateLockScreen = true,
                    goingBack = goingBack,
                    onNumberClick = { enterNumber ->
                        handleCreatePinInput(
                            enterNumber = enterNumber,
                            inputPinCode = inputPinCode,
                            repeatInputPinCode = repeatInputPinCode,
                            onBack = goToBack,
                            onResetError = { isError = false },
                        )
                    },
                )

                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}

/**
 * Обработка ввода при создании PIN-кода.
 */
private fun handleCreatePinInput(
    enterNumber: String,
    inputPinCode: MutableList<Int>,
    repeatInputPinCode: MutableList<Int>,
    onBack: () -> Unit,
    onResetError: () -> Unit,
) {
    when (enterNumber) {
        "<" -> onBack()
        "X" -> {
            // Удаляем из активного списка
            if (inputPinCode.size < 4 || repeatInputPinCode.isEmpty()) {
                if (inputPinCode.isNotEmpty()) {
                    inputPinCode.removeAt(inputPinCode.lastIndex)
                }
            } else {
                repeatInputPinCode.removeAt(repeatInputPinCode.lastIndex)
            }
        }
        else -> {
            // Сбрасываем ошибку при вводе
            if (inputPinCode.size == 1 || repeatInputPinCode.size == 1) {
                onResetError()
            }

            // Добавляем в нужный список
            if (inputPinCode.size < 4) {
                inputPinCode.add(enterNumber.toInt())
            } else if (repeatInputPinCode.size < 4) {
                repeatInputPinCode.add(enterNumber.toInt())
            }
        }
    }
}
