package com.profpay.wallet.presentation.ui.navigation.graphs

import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.profpay.wallet.presentation.ui.navigation.Route
import com.profpay.wallet.presentation.ui.screens.NotNetworkScreen
import com.profpay.wallet.presentation.ui.screens.createOrRecoveryWallet.WelcomingScreen
import com.profpay.wallet.presentation.ui.screens.lockScreen.CreateLockScreen
import com.profpay.wallet.presentation.ui.screens.lockScreen.LockScreen
import com.profpay.wallet.presentation.viewmodel.pinlock.PinLockViewModel

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    pinLockViewModel: PinLockViewModel,
) {
    composable<Route.Welcome> {
        WelcomingScreen(
            goToCreateOrRecovery = {
                navController.navigate(Route.OnboardingGraph) {
                    popUpTo(Route.Welcome) { inclusive = true }
                    launchSingleTop = true
                }
            },
            goToHome = {
                navController.navigate(Route.MainGraph) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }

    composable<Route.PinCreate> {
        CreateLockScreen(
            viewModel = pinLockViewModel,
            toNavigate = {
                // Навигация произойдёт автоматически через navigationEvents
            },
        )
        BackHandler { /* Block back */ }
    }

    composable<Route.PinUnlock> {
        LockScreen(
            viewModel = pinLockViewModel,
            toNavigate = {
                // Навигация произойдёт автоматически через navigationEvents
            },
        )
        BackHandler { /* Block back */ }
    }

    composable<Route.NoNetwork> {
        NotNetworkScreen()
        BackHandler { /* Block back */ }
    }
}
