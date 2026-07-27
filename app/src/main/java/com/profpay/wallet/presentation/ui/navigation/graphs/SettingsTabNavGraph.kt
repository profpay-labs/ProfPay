package com.profpay.wallet.presentation.ui.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.profpay.wallet.presentation.ui.navigation.Route
import com.profpay.wallet.presentation.ui.screens.lockScreen.CreateLockScreen
import com.profpay.wallet.presentation.ui.screens.lockScreen.LockScreen
import com.profpay.wallet.presentation.ui.screens.settings.SettingsAccountScreen
import com.profpay.wallet.presentation.ui.screens.settings.SettingsAmlScreen
import com.profpay.wallet.presentation.ui.screens.settings.SettingsNotificationsScreen
import com.profpay.wallet.presentation.ui.screens.settings.SettingsScreen
import com.profpay.wallet.presentation.ui.screens.settings.SettingsSecurityScreen

fun NavGraphBuilder.settingsTabNavGraph(navController: NavController) {
    // Используем SettingsGraph как route для navigation графа
    // и SettingsMain как startDestination
    navigation<Route.SettingsGraph>(
        startDestination = Route.Settings,
    ) {
        composable<Route.Settings> {
            SettingsScreen(
                goToSettingsNotifications = {
                    navController.navigate(Route.SettingsNotifications)
                },
                goToSettingsSecurity = {
                    navController.navigate(Route.SettingsSecurity)
                },
                goToSettingsAccount = {
                    navController.navigate(Route.SettingsAccount)
                },
                goToSettingsAml = {
                    navController.navigate(Route.SettingsAml)
                },
            )
        }

        composable<Route.SettingsAccount> {
            SettingsAccountScreen(
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.SettingsNotifications> {
            SettingsNotificationsScreen(
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.SettingsSecurity> {
            SettingsSecurityScreen(
                goToBack = { navController.navigateUp() },
                goToLock = { navController.navigate(Route.SettingsPinChange) },
            )
        }

        composable<Route.SettingsAml> {
            SettingsAmlScreen(
                goToBack = { navController.navigateUp() },
            )
        }

        composable<Route.SettingsPinChange> {
            LockScreen(
                toNavigate = {
                    navController.navigate(Route.SettingsPinCreate)
                },
                goingBack = true,
                goToBack = {
                    navController.navigate(Route.Settings) {
                        popUpTo<Route.Settings> { inclusive = false }
                    }
                },
            )
        }

        composable<Route.SettingsPinCreate> {
            CreateLockScreen(
                toNavigate = {
                    navController.navigate(Route.Settings) {
                        popUpTo<Route.Wallet> { inclusive = false }
                    }
                },
                goingBack = true,
                goToBack = {
                    navController.navigate(Route.Settings) {
                        popUpTo<Route.Settings> { inclusive = false }
                    }
                },
            )
        }
    }
}
