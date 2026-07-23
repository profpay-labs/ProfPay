package com.profpay.wallet.ui.navigation.graphs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.profpay.wallet.ui.navigation.Route
import com.profpay.wallet.ui.screens.wallet.CentralAddressTxHistoryScreen
import com.profpay.wallet.ui.screens.wallet.ReceiveFromWalletSotsScreen
import com.profpay.wallet.ui.screens.wallet.SendFromWalletInfoScreen
import com.profpay.wallet.ui.screens.wallet.TXDetailsScreen
import com.profpay.wallet.ui.screens.wallet.WalletAddressScreen
import com.profpay.wallet.ui.screens.wallet.WalletArchivalSotsScreen
import com.profpay.wallet.ui.screens.wallet.WalletInfoScreen
import com.profpay.wallet.ui.screens.wallet.WalletSotsScreen
import com.profpay.wallet.ui.screens.wallet.WalletSystemScreen
import com.profpay.wallet.ui.screens.wallet.WalletSystemTRXScreen

@RequiresApi(Build.VERSION_CODES.S)
fun NavGraphBuilder.walletTabNavGraph(navController: NavController) {
    navigation<Route.Wallet>(
        startDestination = Route.WalletDetails,
    ) {
        // Main Wallet screen
        composable<Route.WalletDetails> {
            WalletInfoScreen(
                goToSendWalletInfo = { addressId, tokenName ->
                    navController.navigate(Route.WalletSend(addressId, tokenName))
                },
                goToWalletSystem = {
                    navController.navigate(Route.WalletSystem)
                },
                goToWalletSystemTRX = {
                    navController.navigate(Route.SystemTrx)
                },
                goToWalletSots = {
                    navController.navigate(Route.WalletSots)
                },
                goToTXDetailsScreen = {
                    navController.navigate(Route.TransactionDetails)
                },
            )
        }

        // Wallet Sots screen
        composable<Route.WalletSots> {
            WalletSotsScreen(
                goToWalletAddress = {
                    // Note: WalletAddressScreen читает address из SharedPreferences
                    navController.navigate(Route.WalletAddress("", ""))
                },
                goToWalletArchivalSots = {
                    navController.navigate(Route.WalletArchivalSots)
                },
                goToBack = { navController.navigateUp() },
                goToReceive = {
                    navController.navigate(Route.WalletReceive(""))
                },
            )
        }

        // Archival Sots
        composable<Route.WalletArchivalSots> {
            WalletArchivalSotsScreen(
                goToBack = { navController.navigateUp() },
                goToWalletAddress = {
                    navController.navigate(Route.WalletAddress("", ""))
                },
            )
        }

        // Wallet Address screen
        composable<Route.WalletAddress> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.WalletAddress>()
            WalletAddressScreen(
                goToSendWalletAddress = { addressId, tokenName ->
                    navController.navigate(Route.WalletSend(addressId, tokenName))
                },
                goToBack = { navController.navigateUp() },
                goToSystemTRX = { navController.navigate(Route.SystemTrx) },
                goToTXDetailsScreen = { navController.navigate(Route.TransactionDetails) },
                goToReceive = { navController.navigate(Route.WalletReceive(route.address)) },
            )
        }

        // Send from wallet
        composable<Route.WalletSend> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.WalletSend>()
            SendFromWalletInfoScreen(
                addressId = route.addressId,
                tokenName = route.tokenName,
                goToBack = { navController.navigateUp() },
                goToSystemTRX = { navController.navigate(Route.SystemTrx) },
            )
        }

        // Receive screen
        composable<Route.WalletReceive> {
            ReceiveFromWalletSotsScreen(
                goToBack = { navController.navigateUp() },
            )
        }

        // Wallet System
        composable<Route.WalletSystem> {
            WalletSystemScreen(
                goToBack = { navController.navigateUp() },
                goToWalletInfo = {
                    navController.navigate(Route.Wallet) {
                        popUpTo<Route.Wallet> { inclusive = false }
                    }
                },
                goToCoRA = {
                    navController.navigate(Route.WalletSystemOnboardingGraph)
                },
            )
        }

        // System TRX
        composable<Route.SystemTrx> {
            WalletSystemTRXScreen(
                goToBack = { navController.navigateUp() },
                goToCentralAddressTxHistory = {
                    navController.navigate(Route.CentralAddressTransactionHistory)
                },
            )
        }

        // Central Address Tx History
        composable<Route.CentralAddressTransactionHistory> {
            CentralAddressTxHistoryScreen(
                goToBack = { navController.navigateUp() },
            )
        }

        // Transaction Details
        composable<Route.TransactionDetails> {
            TXDetailsScreen(
                goToBack = { navController.navigateUp() },
            )
        }

        // Create or Recovery from Wallet System
        walletSetupNavGraph(navController)
    }
}
