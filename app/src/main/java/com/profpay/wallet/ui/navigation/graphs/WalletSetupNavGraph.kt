package com.profpay.wallet.ui.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.profpay.wallet.ui.navigation.Route
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.CreateNewWalletScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.CreateOrRecoverWalletScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.CreatedWalletAddingScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.RecoverWalletScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.RecoveringWalletAddingScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.SeedPhraseConfirmationScreen

fun NavGraphBuilder.walletSetupNavGraph(navController: NavController) {
    navigation<Route.WalletSystemOnboardingGraph>(
        startDestination = Route.WalletSystemOnboardingChoice,
    ) {
        composable<Route.WalletSystemOnboardingChoice> {
            CreateOrRecoverWalletScreen(
                goToCreateNewWallet = {
                    navController.navigate(Route.CreateWalletFromWalletSystem)
                },
                goToRecoverWallet = {
                    navController.navigate(Route.RecoverWalletFromWalletSystem)
                },
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.CreateWalletFromWalletSystem> {
            CreateNewWalletScreen(
                goToSeedPhraseConfirmation = {
                    navController.navigate(Route.SeedPhraseConfirmationFromWalletSystem)
                },
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.RecoverWalletFromWalletSystem> {
            RecoverWalletScreen(
                goToRecoveringWalletAdding = {
                    navController.navigate(Route.RecoveringWalletAddingFromWalletSystem)
                },
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.SeedPhraseConfirmationFromWalletSystem> {
            SeedPhraseConfirmationScreen(
                goToWalletAdded = {
                    navController.navigate(Route.WalletAddedFromWalletSystem)
                },
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.WalletAddedFromWalletSystem> {
            CreatedWalletAddingScreen(
                goToHome = {
                    navController.navigate(Route.Wallet) {
                        popUpTo<Route.Wallet> { inclusive = false }
                    }
                },
            )
        }

        composable<Route.RecoveringWalletAddingFromWalletSystem> {
            RecoveringWalletAddingScreen(
                goToHome = {
                    navController.navigate(Route.Wallet) {
                        popUpTo<Route.Wallet> { inclusive = false }
                    }
                },
            )
        }
    }
}
