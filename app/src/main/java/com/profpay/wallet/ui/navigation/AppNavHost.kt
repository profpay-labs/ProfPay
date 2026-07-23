package com.profpay.wallet.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.profpay.core.network.monitor.NetworkMonitor
import com.profpay.domain.user.model.AppState
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.bridge.viewmodel.AppStateUiState
import com.profpay.wallet.bridge.viewmodel.AppStateViewModel
import com.profpay.wallet.bridge.viewmodel.pinlock.LockState
import com.profpay.wallet.bridge.viewmodel.pinlock.PinLockViewModel
import com.profpay.wallet.ui.navigation.graphs.authNavGraph
import com.profpay.wallet.ui.navigation.graphs.mainNavGraph
import com.profpay.wallet.ui.navigation.graphs.onboardingNavGraph
import com.profpay.wallet.ui.screens.SplashScreen
import com.profpay.wallet.ui.shared.sharedPref
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AppNavHost(
    navController: NavHostController,
    networkMonitor: NetworkMonitor,
    pinLockViewModel: PinLockViewModel = hiltViewModel(),
    appStateViewModel: AppStateViewModel = hiltViewModel(),
) {
    val sharedPref = sharedPref()

    // SharedPreferences только для UX-флагов (показ welcome экрана)
    val isAcceptedRules = sharedPref.getBoolean(PrefKeys.ACCEPTED_RULES, false)

    // Проверяем PIN-состояние при первом запуске
    LaunchedEffect(Unit) {
        pinLockViewModel.checkPinState()
    }

    // Обработка navigation: ждём ОБА состояния
    LaunchedEffect(Unit) {
        combine(
            pinLockViewModel.navigationEvents,
            appStateViewModel.appState
        ) { lockState, appStateUi ->
            Pair(lockState, appStateUi)
        }
            .filter { (_, appStateUi) -> appStateUi is AppStateUiState.Loaded }
            .collect { (lockState, appStateUi) ->
                val currentAppState = (appStateUi as AppStateUiState.Loaded).appState

                val targetRoute: Route = when (lockState) {
                    LockState.RequireCreation -> Route.PinCreate
                    LockState.RequireUnlock -> Route.PinUnlock
                    LockState.None -> determineStartRoute(
                        appState = currentAppState,
                        isAcceptedRules = isAcceptedRules,
                    )
                }

                val currentRoute = navController.currentDestination?.route
                if (currentRoute != targetRoute::class.qualifiedName) {
                    if (lockState == LockState.None) {
                        // При разблокировке очищаем backstack и переходим
                        navController.navigate(targetRoute) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(targetRoute) {
                            launchSingleTop = true
                        }
                    }
                }
            }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Splash,
    ) {
        // Splash
        composable<Route.Splash> {
            SplashScreen()
        }

        // Auth screens (Lock, Create Lock)
        authNavGraph(navController, pinLockViewModel)

        // Main app (Bottom bar + nested graphs)
        mainNavGraph(navController, networkMonitor)

        // Onboarding / Recovery
        onboardingNavGraph(navController)
    }
}

/**
 * Определяет стартовый route на основе AppState.
 *
 * Логика:
 * - Не принял соглашение → Welcome
 * - Не зарегистрирован → OnboardingGraph
 * - Зарегистрирован, но нет кошельков → OnboardingGraph (нужно создать кошелёк)
 * - Зарегистрирован и есть кошельки → MainGraph
 */
private fun determineStartRoute(
    appState: AppState,
    isAcceptedRules: Boolean,
): Route {
    // 1. Соглашение не принято — показываем welcome
    if (!isAcceptedRules) {
        return Route.Welcome
    }

    // 2. Определяем по AppState
    return when (appState) {
        is AppState.NotRegistered -> Route.OnboardingGraph

        is AppState.Registered -> {
            if (appState.hasWallets) {
                Route.MainGraph
            } else {
                // Зарегистрирован, но кошельков нет (edge case)
                Route.OnboardingGraph
            }
        }
    }
}
