package com.profpay.wallet.ui.screens.lockScreen

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
import com.profpay.wallet.bridge.viewmodel.pinlock.PinLockViewModel
import com.profpay.wallet.bridge.viewmodel.pinlock.PinUiState
import com.profpay.wallet.ui.feature.lockScreen.InputDots
import com.profpay.wallet.ui.feature.lockScreen.NumberBoard
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LockScreen(
    toNavigate: () -> Unit,
    viewModel: PinLockViewModel = hiltViewModel(),
    goToBack: () -> Unit = {},
    goingBack: Boolean = false,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val inputPinCode = remember { mutableStateListOf<Int>() }
    var isError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Обработка состояний от ViewModel
    LaunchedEffect(uiState) {
        when (uiState) {
            PinUiState.Success -> {
                toNavigate()
            }
            PinUiState.ValidationFailed -> {
                isError = true
                inputPinCode.clear()
                viewModel.resetState()
            }
            is PinUiState.Error -> {
                val errorMessage = (uiState as PinUiState.Error).message
                snackbarHostState.showSnackbar(errorMessage)
                isError = true
                inputPinCode.clear()
                viewModel.resetState()
            }
            else -> { /* Idle, Loading */ }
        }
    }

    // Автоматическая валидация при вводе 4 цифр
    LaunchedEffect(inputPinCode.size) {
        if (inputPinCode.size == 4) {
            delay(250.milliseconds)
            val pinCode = inputPinCode.joinToString(separator = "")
            viewModel.validatePin(pinCode)
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
                Text(text = "Verify 4-digit security PIN")

                Spacer(modifier = Modifier.fillMaxHeight(0.02f))

                InputDots(
                    inputPinCode,
                    isError,
                    onErrorReset = { isError = false },
                )

                NumberBoard(
                    inputPinCode = inputPinCode,
                    goingBack = goingBack,
                    onNumberClick = { enterNumber ->
                        handleNumberInput(
                            enterNumber = enterNumber,
                            inputPinCode = inputPinCode,
                            onBack = goToBack,
                            onResetError = { isError = false },
                        )
                    },
                    onClickBiom = {
                        viewModel.unlockWithBiometric()
                    },
                )

                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}

/**
 * Обработка ввода с цифровой клавиатуры.
 */
private fun handleNumberInput(
    enterNumber: String,
    inputPinCode: MutableList<Int>,
    onBack: () -> Unit,
    onResetError: () -> Unit,
) {
    when (enterNumber) {
        "" -> Unit
        "<" -> onBack()
        "-1" -> Unit // Биометрия обрабатывается отдельно
        "X" -> {
            if (inputPinCode.isNotEmpty()) {
                inputPinCode.removeAt(inputPinCode.lastIndex)
            }
        }
        else -> {
            if (inputPinCode.size == 1) {
                onResetError()
            }
            if (inputPinCode.size < 4) {
                inputPinCode.add(enterNumber.toInt())
            }
        }
    }
}
