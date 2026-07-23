package com.profpay.wallet

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.profpay.core.network.auth.WalletAuthProvider
import com.profpay.core.network.monitor.NetworkMonitor
import com.profpay.wallet.bridge.viewmodel.pinlock.PinLockViewModel
import com.profpay.wallet.bridge.viewmodel.settings.ThemeState
import com.profpay.wallet.bridge.viewmodel.settings.ThemeViewModel
import com.profpay.core.security.lock.AppLockManager
import com.profpay.wallet.ui.app.theme.WalletNavigationBottomBarTheme
import com.profpay.wallet.ui.navigation.AppNavHost
import com.profpay.wallet.ui.screens.NotNetworkScreen
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import kotlinx.coroutines.launch
import me.pushy.sdk.util.exceptions.PushyNetworkException
import javax.inject.Inject

/**
 * Main Activity приложения.
 * Отвечает за:
 * - Инициализацию приложения
 * - Управление темой
 * - Lifecycle наблюдение (блокировка приложения)
 * - Отображение основного или error контента
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appInitializer: AppInitializer

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var walletAuthProvider: WalletAuthProvider

    @Inject
    lateinit var appLockManager: AppLockManager

    private val pinLockViewModel: PinLockViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLifecycleObserver()
        enableEdgeToEdge()

        lifecycleScope.launch {
            launchApp()
        }
    }

    private fun setupLifecycleObserver() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(
                onAppForegrounded = { pinLockViewModel.checkPinState() },
                onAppBackgrounded = {
                    appLockManager.lock()
                    walletAuthProvider.clearPrivateKeyCache()
                },
            ),
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun launchApp() {
        try {
            appInitializer.initialize()
            setContent { MainContent() }
        } catch (e: PushyNetworkException) {
            Sentry.captureException(e)
            setContent { NetworkErrorContent() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun MainContent() {
        ThemedContent { isDarkTheme ->
            WalletNavigationBottomBarTheme(activity = this, isDarkTheme = isDarkTheme) {
                val navController = rememberNavController()
                AppNavHost(navController, networkMonitor)
            }
        }
    }

    @Composable
    private fun NetworkErrorContent() {
        ThemedContent { isDarkTheme ->
            WalletNavigationBottomBarTheme(activity = this, isDarkTheme = isDarkTheme) {
                NotNetworkScreen()
            }
        }
    }

    /**
     * Обёртка для применения темы.
     * Загружает настройки темы и передаёт isDarkTheme в content.
     */
    @Composable
    private fun ThemedContent(content: @Composable (isDarkTheme: Boolean) -> Unit) {
        val themeViewModel: ThemeViewModel = hiltViewModel()
        val state by themeViewModel.state.collectAsStateWithLifecycle()
        val isSystemDark = isSystemInDarkTheme()

        // Используем системную тему как fallback при загрузке
        val isDarkTheme = when (val themeState = state) {
            is ThemeState.Loading -> isSystemDark
            is ThemeState.Success -> themeViewModel.isDarkTheme(
                themeState.themeStateResult,
                isSystemDark,
            )
        }

        content(isDarkTheme)
    }
}
