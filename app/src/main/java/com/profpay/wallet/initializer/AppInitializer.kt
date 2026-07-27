package com.profpay.wallet.initializer

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.bugfender.sdk.Bugfender
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.domain.market.model.BinanceSymbol
import com.profpay.domain.market.model.CoinSymbol
import com.profpay.domain.market.model.local.ExchangeRateLocal
import com.profpay.domain.market.model.local.TradingInsightsLocal
import com.profpay.domain.market.repository.ExchangeRatesLocalRepository
import com.profpay.domain.market.repository.MarketRepository
import com.profpay.domain.market.repository.TradingInsightsLocalRepository
import com.profpay.domain.user.model.AppState
import com.profpay.domain.user.repository.AppStateRepository
import com.profpay.domain.wallet.model.local.CentralAddressLocal
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.wallet.PrefKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import me.pushy.sdk.Pushy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appStateRepository: AppStateRepository,
    private val sharedPrefs: SharedPreferences,
    private val exchangeRatesLocalRepository: ExchangeRatesLocalRepository,
    private val tradingInsightsLocalRepository: TradingInsightsLocalRepository,
    private val tron: Tron,
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
    private val marketRepository: MarketRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Инициализирует приложение:
     * - Регистрирует устройство в Pushy (при первом запуске)
     * - Создаёт central address (при первом запуске)
     * - Синхронизирует курсы валют
     *
     * @throws PushyNetworkException если нет сети при первом запуске
     */
    suspend fun initialize() {
        val appState = appStateRepository.getAppState()
        val isFirstStart = appState is AppState.NotRegistered

        setupBugfender(isFirstStart)

        if (isFirstStart) {
            performFirstStartSetup()
        }

        // Синхронизация курсов в фоне (не блокируем UI)
        supervisorScope {
            syncExchangeRatesAndTrends()
        }
    }

    private fun setupBugfender(isFirstStart: Boolean) {
        Bugfender.setDeviceBoolean("app.first_started", isFirstStart)

        sharedPrefs.getString(PrefKeys.DEVICE_TOKEN, null)?.let { token ->
            Bugfender.setDeviceString("pushy.device_token", token)
        }
    }

    private suspend fun performFirstStartSetup() {
        // Регистрация в Pushy
        val deviceToken = withContext(ioDispatcher) {
            Pushy.register(context)
        }

        // Создание central address
        val address = withContext(ioDispatcher) {
            tron.addressUtilities.generateSingleAddress()
        }

        centralAddressLocalRepository.insert(
            CentralAddressLocal(
                address = address.address,
                publicKey = address.publicKey,
                privateKey = address.privateKey,
            ),
        )

        // Сохраняем device token
        sharedPrefs.edit {
            putString(PrefKeys.DEVICE_TOKEN, deviceToken)
        }

        Bugfender.setDeviceString("pushy.device_token", deviceToken)
        Bugfender.setDeviceBoolean("user.is.first_started", true)
        Bugfender.setDeviceBoolean("app.initialized", true)
    }

    private suspend fun syncExchangeRatesAndTrends() = withContext(ioDispatcher) {
        // Запускаем синхронизацию параллельно
        val jobs = listOf(
            async { syncExchangeRates() },
            async { syncTradingInsights() },
        )

        // Ждём завершения, но не падаем при ошибках
        jobs.awaitAll()
    }

    private suspend fun syncExchangeRates() {
        BinanceSymbol.entries.forEach { symbol ->
            marketRepository.getExchangeRate(symbol)
                .onSuccess { rate ->
                    upsertExchangeRate(symbol.symbol, rate.price.toDouble())
                }
                .onFailure { e ->
                    Sentry.captureException(e)
                    // Используем дефолтное значение при ошибке
                    upsertExchangeRate(symbol.symbol, 1.0)
                }
        }
    }

    private suspend fun syncTradingInsights() {
        CoinSymbol.entries.forEach { symbol ->
            marketRepository.getPriceChange24h(symbol)
                .onSuccess { priceChange ->
                    upsertTradingInsight(symbol.id, priceChange.percentageChange)
                }
                .onFailure { e ->
                    Sentry.captureException(e)
                    upsertTradingInsight(symbol.id, 0.0)
                }
        }
    }

    private suspend fun upsertExchangeRate(symbol: String, value: Double) {
        if (exchangeRatesLocalRepository.exists(symbol)) {
            exchangeRatesLocalRepository.update(symbol = symbol, rate = value)
        } else {
            exchangeRatesLocalRepository.insert(ExchangeRateLocal(symbol = symbol, rate = value))
        }
    }

    private suspend fun upsertTradingInsight(symbol: String, priceChange: Double) {
        if (tradingInsightsLocalRepository.exists(symbol)) {
            tradingInsightsLocalRepository.updatePriceChange24h(
                symbol = symbol,
                percentage = priceChange,
            )
        } else {
            tradingInsightsLocalRepository.insert(
                TradingInsightsLocal(
                    symbol = symbol,
                    priceChangePercentage24h = priceChange,
                ),
            )
        }
    }
}
