package com.profpay.wallet.ui.navigation.graphs

import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.profpay.wallet.ui.navigation.Route
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.CreateNewWalletScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.CreateOrRecoverWalletScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.CreatedWalletAddingScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.RecoverWalletScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.RecoveringWalletAddingScreen
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.SeedPhraseConfirmationScreen

fun NavGraphBuilder.onboardingNavGraph(navController: NavController) {
    navigation<Route.OnboardingGraph>(
        startDestination = Route.OnboardingChoice,
    ) {
        composable<Route.OnboardingChoice> {
            CreateOrRecoverWalletScreen(
                goToCreateNewWallet = {
                    navController.navigate(Route.CreateWallet)
                },
                goToRecoverWallet = {
                    navController.navigate(Route.RecoverWallet)
                },
                goToBack = {},
            )
            BackHandler { /* Block back */ }
        }

        composable<Route.CreateWallet> {
            CreateNewWalletScreen(
                goToSeedPhraseConfirmation = {
                    navController.navigate(Route.SeedPhraseConfirmation)
                },
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.RecoverWallet> {
            RecoverWalletScreen(
                goToRecoveringWalletAdding = {
                    navController.navigate(Route.WalletRecoveryProgress)
                },
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.SeedPhraseConfirmation> {
            SeedPhraseConfirmationScreen(
                goToWalletAdded = {
                    navController.navigate(Route.WalletCreated)
                },
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.WalletCreated> {
            CreatedWalletAddingScreen(
                goToHome = {
                    navController.navigate(Route.MainGraph) {
                        popUpTo<Route.OnboardingGraph> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.WalletRecoveryProgress> {
            RecoveringWalletAddingScreen(
                goToHome = {
                    navController.navigate(Route.MainGraph) {
                        popUpTo<Route.OnboardingGraph> { inclusive = true }
                    }
                },
            )
        }
    }
}
