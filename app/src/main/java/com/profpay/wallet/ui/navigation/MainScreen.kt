package com.profpay.wallet.ui.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.profpay.wallet.ui.navigation.graphs.walletTabNavGraph
import com.profpay.wallet.ui.navigation.graphs.settingsTabNavGraph
import com.profpay.wallet.ui.navigation.graphs.smartContractsTabNavGraph
import com.profpay.wallet.ui.shared.sharedPref

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(navController: NavHostController = rememberNavController()) {
    Scaffold(
        bottomBar = {
            HomeBottomBar(navController = navController)
        },
    ) { padding ->
        sharedPref().edit { putFloat("bottomPadding", padding.calculateBottomPadding().value) }

        HomeNavHost(navController = navController)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun HomeNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.Wallet,
    ) {
        // Smart Contracts tab
        smartContractsTabNavGraph(navController)

        // Wallet/Profile tab
        walletTabNavGraph(navController)

        // Settings tab
        settingsTabNavGraph(navController)
    }
}

@Composable
private fun HomeBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Проверяем, нужно ли показывать bottom bar
    val showBottomBar = shouldShowBottomBar(currentDestination)

    if (showBottomBar) {
        BottomAppBar(
            modifier = Modifier.padding(),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            BottomBarTab.entries.forEach { tab ->
                val selected = isTabSelected(currentDestination, tab)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    // Индикатор выбранного таба
                    if (selected) {
                        Canvas(modifier = Modifier.size(width = 70.dp, height = 0.dp)) {
                            val canvasWidth = size.width
                            drawLine(
                                start = Offset(x = canvasWidth, y = -8f),
                                end = Offset(x = 0f, y = -8f),
                                strokeWidth = 10f,
                                color = androidx.compose.ui.graphics.Color.White,
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (selected) {
                                // Если таб уже выбран - сбрасываем на корневой экран этого таба
                                // Используем popBackStack с проверкой: если уже на корне - ничего не делаем
                                val tabRootRoute = getTabRootRoute(tab)
                                val isAlreadyAtRoot = currentDestination?.hasRoute(tabRootRoute::class) == true

                                if (!isAlreadyAtRoot) {
                                    // Навигируем на корневой экран таба, очищая внутренний backstack
                                    navController.navigate(tabRootRoute) {
                                        popUpTo(tabRootRoute) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                                // Если уже на корне таба - ничего не делаем
                            } else {
                                // Переключаемся на другой таб
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = tab.iconRes),
                            contentDescription = tab.title,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            },
                        )
                    }

                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        },
                    )
                }
            }
        }
    }
}

/**
 * Возвращает корневой route для каждого таба.
 * Нужно для правильного сброса backstack внутри таба.
 */
private fun getTabRootRoute(tab: BottomBarTab): Route {
    return when (tab) {
        BottomBarTab.Profile -> Route.Wallet
        BottomBarTab.SmartContracts -> Route.SmartContractList
        BottomBarTab.Settings -> Route.Settings // Корневой экран внутри SettingsGraph
    }
}

private fun shouldShowBottomBar(destination: NavDestination?): Boolean {
    if (destination == null) return false

    // Показываем bottom bar для основных табов и их вложенных экранов
    return destination.hierarchy.any { dest ->
        dest.hasRoute<Route.Wallet>() ||
            dest.hasRoute<Route.SmartContractList>() ||
            dest.hasRoute<Route.Settings>() ||
            dest.hasRoute<Route.WalletDetails>() ||
            dest.hasRoute<Route.WalletSots>() ||
            dest.hasRoute<Route.SettingsNotifications>() ||
            dest.hasRoute<Route.SettingsSecurity>() ||
            dest.hasRoute<Route.SettingsAccount>() ||
            dest.hasRoute<Route.SettingsAml>()
    }
}

private fun isTabSelected(destination: NavDestination?, tab: BottomBarTab): Boolean {
    if (destination == null) return false

    return when (tab) {
        BottomBarTab.Profile -> destination.hierarchy.any {
            it.hasRoute<Route.Wallet>() ||
                it.hasRoute<Route.WalletDetails>() ||
                it.hasRoute<Route.WalletSots>()
        }
        BottomBarTab.SmartContracts -> destination.hierarchy.any {
            it.hasRoute<Route.SmartContractList>()
        }
        BottomBarTab.Settings -> destination.hierarchy.any {
            it.hasRoute<Route.SettingsGraph>() ||
                it.hasRoute<Route.Settings>() ||
                it.hasRoute<Route.SettingsNotifications>() ||
                it.hasRoute<Route.SettingsSecurity>() ||
                it.hasRoute<Route.SettingsAccount>() ||
                it.hasRoute<Route.SettingsAml>()
        }
    }
}
