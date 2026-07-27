package com.profpay.wallet.presentation.ui.screens.createOrRecoveryWallet

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.domain.wallet.model.RecoveredAddressData
import com.profpay.domain.wallet.model.RecoveryResult
import com.profpay.wallet.R
import com.profpay.wallet.presentation.ui.shared.sharedPref
import com.profpay.wallet.presentation.ui.theme.BackgroundDark
import com.profpay.wallet.presentation.ui.theme.BackgroundLight
import com.profpay.wallet.presentation.viewmodel.createorrecovery.RecoverWalletState
import com.profpay.wallet.presentation.viewmodel.createorrecovery.RecoverWalletViewModel
import com.profpay.wallet.presentation.viewmodel.createorrecovery.WalletAddedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveringWalletAddingScreen(
    viewModel: RecoverWalletViewModel = hiltViewModel(),
    goToHome: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
        ) {}
        when (state) {
            is RecoverWalletState.Loading ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .paint(
                            painterResource(id = R.drawable.create_recovery_bg_end),
                            contentScale = ContentScale.FillBounds,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            is RecoverWalletState.Success -> {
                val successState = state as RecoverWalletState.Success
                when (val result = successState.recoveryResult) {
                    is RecoveryResult.Success -> {
                        RecoveringWalletAddingWidget(
                            addressData = result.addressData,  // ← Изменено с address на addressData
                            goToHome = goToHome,
                            accountWasFound = result.accountWasFound,
                            userId = result.userId,
                            clearState = { viewModel.clearRecoveryResult() },
                        )
                    }
                    is RecoveryResult.AddressNotFound -> { /* handle */ }
                    is RecoveryResult.Error -> { /* handle */ }
                    is RecoveryResult.InvalidMnemonic -> { /* handle */ }
                    is RecoveryResult.RepeatingMnemonic -> { /* handle */ }
                    is RecoveryResult.Empty -> { /* handle */ }
                }
            }
        }
    }
}

@Composable
fun RecoveringWalletAddingWidget(
    viewModel: WalletAddedViewModel = hiltViewModel(),  // ← Добавить ViewModel
    addressData: RecoveredAddressData,
    goToHome: () -> Unit,
    accountWasFound: Boolean,
    userId: Long?,
    clearState: () -> Unit,
) {
    val sharedPref = sharedPref()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is WalletAddedViewModel.WalletUiEvent.NavigateToHome -> {
                    clearState()
                    goToHome()
                }
                is WalletAddedViewModel.WalletUiEvent.ShowError -> {
                    clearState()
                    Log.e("WalletAdded", "Error: ${event.message}")
                }
                null -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painterResource(id = R.drawable.create_recovery_bg_end),
                contentScale = ContentScale.FillBounds,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 70.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                modifier = Modifier.size(170.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.icon_smart),
                contentDescription = "",
                tint = BackgroundLight,
            )
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 18.dp),
                text = "Всё готово!",
                style = MaterialTheme.typography.displayMedium,
                color = BackgroundLight,
            )
            Button(
                onClick = {
                    if (accountWasFound && userId == null) {
                        throw Exception("User ID not found.")
                    }

                    viewModel.onWalletRecoveryClicked(
                        sharedPref = sharedPref,
                        addressData = addressData,
                        accountWasFound = accountWasFound,
                        userId = userId,
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BackgroundLight),
                modifier = Modifier.padding(vertical = 16.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = "Приступить к работе",
                    style = MaterialTheme.typography.titleSmall,
                    color = BackgroundDark,
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.97f),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "ProfPay",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = BackgroundDark,
        )
        Text(
            text = "ProfPay IO, 2024",
            style = MaterialTheme.typography.titleSmall,
            color = BackgroundDark,
        )
    }
}
