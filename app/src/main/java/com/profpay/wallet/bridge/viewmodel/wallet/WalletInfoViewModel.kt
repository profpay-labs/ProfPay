package com.profpay.wallet.bridge.viewmodel.wallet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.converter.toSunAmount
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.data.transfer.service.TransactionSyncService
import com.profpay.domain.aml.repository.AmlRepository
import com.profpay.domain.market.model.BinanceSymbol
import com.profpay.domain.market.model.CoinSymbol
import com.profpay.domain.market.repository.ExchangeRatesLocalRepository
import com.profpay.domain.market.repository.TradingInsightsLocalRepository
import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.ActiveWalletManager
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.domain.wallet.model.local.TokenLocal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.domain.wallet.repository.local.TokenLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import com.profpay.domain.wallet.repository.local.WalletProfileLocalRepository
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class WalletInfoViewModel @Inject constructor(
    private val walletProfileLocalRepository: WalletProfileLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
    private val addressLocalRepository: AddressLocalRepository,
    private val tokenLocalRepository: TokenLocalRepository,
    val exchangeRatesLocalRepository: ExchangeRatesLocalRepository,
    val tradingInsightsLocalRepository: TradingInsightsLocalRepository,
    private val tron: Tron,
    var profileLocalRepository: ProfileLocalRepository,
    var centralAddressLocalRepository: CentralAddressLocalRepository,
    var pendingTransactionLocalRepository: PendingTransactionLocalRepository,
    private val amlRepository: AmlRepository,
    private val activeWalletManager: ActiveWalletManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _walletName = MutableStateFlow<String?>(null)
    val walletName: StateFlow<String?> = _walletName.asStateFlow()

    private val _tokensWithTotalBalance = MutableStateFlow<List<TokenLocal>>(emptyList())
    val tokensWithTotalBalance: StateFlow<List<TokenLocal>> = _tokensWithTotalBalance.asStateFlow()

    private val _totalBalance = MutableStateFlow<BigInteger>(BigInteger.ZERO)
    val totalBalance: StateFlow<BigInteger> = _totalBalance.asStateFlow()

    private val _totalPercentage24h = MutableStateFlow(0.0)
    val totalPercentage24h: StateFlow<Double> = _totalPercentage24h.asStateFlow()

    private val _transactionsByDate = MutableStateFlow<List<List<TransactionSummary>>>(emptyList())
    val transactionsByDate: StateFlow<List<List<TransactionSummary>>> = _transactionsByDate.asStateFlow()

    private val _trxUsdtRate = MutableStateFlow(BigDecimal.ZERO)
    val trxUsdtRate: StateFlow<BigDecimal> = _trxUsdtRate.asStateFlow()

    fun loadTrxUsdtRate() = viewModelScope.launch(ioDispatcher) {
        val rate = exchangeRatesLocalRepository.getRate(BinanceSymbol.TRX_USDT.symbol)
        _trxUsdtRate.value = rate.toBigDecimal()
    }

    fun syncTransactions() = viewModelScope.launch(ioDispatcher) {
        val walletId = activeWalletManager.activeWalletId
        val trsService = TransactionSyncService(
            addressLocalRepository = addressLocalRepository,
            profileLocalRepository = profileLocalRepository,
            transactionLocalRepository = transactionLocalRepository,
            tokenLocalRepository = tokenLocalRepository,
            centralAddressLocalRepository = centralAddressLocalRepository,
            tron = tron,
            pendingTransactionLocalRepository = pendingTransactionLocalRepository,
            amlRepository = amlRepository
        )
        trsService.startSync(walletId)
    }

    fun getWalletNameById() =
        viewModelScope.launch(ioDispatcher) {
            val walletId = activeWalletManager.activeWalletId
            val name = walletProfileLocalRepository.getNameById(walletId)
            _walletName.emit(name)
        }

    fun getAddressesSotsWithTokens(): LiveData<List<AddressWithTokensLocal>> {
        val walletId = activeWalletManager.activeWalletId
        return liveData(ioDispatcher) {
            emitSource(addressLocalRepository.observeAllSotsWithTokens(walletId).asLiveData())
        }
    }

    fun groupTransactionsByDate(listTransactions: List<TransactionSummary>) =
        viewModelScope.launch(ioDispatcher) {
            if (listTransactions.isEmpty()) {
                _transactionsByDate.value = emptyList()
                return@launch
            }

            val grouped =
                listTransactions
                    .sortedByDescending { it.timestamp }
                    .groupBy { it.transactionDate }
                    .values
                    .toList()

            _transactionsByDate.value = grouped
        }

    fun getAllRelatedTransactions(): LiveData<List<TransactionSummary>> {
        val walletId = activeWalletManager.activeWalletId
        return liveData(ioDispatcher) {
            emitSource(transactionLocalRepository.observeAllByWalletId(walletId).asLiveData())
        }
    }

    fun updateTokenBalances(listAddressWithTokens: List<AddressWithTokensLocal>) =
        viewModelScope.launch(ioDispatcher) {
            if (listAddressWithTokens.isEmpty()) return@launch

            TokenName.entries
                .flatMap { token ->
                    listAddressWithTokens.map { addressWithTokens ->
                        async {
                            val addressId = addressLocalRepository.getByAddress(addressWithTokens.address.address)?.id

                            val balance =
                                if (token == TokenName.USDT) {
                                    tron.addressUtilities.getUsdtBalance(addressWithTokens.address.address)
                                } else {
                                    tron.addressUtilities.getTrxBalance(addressWithTokens.address.address)
                                }
                            tokenLocalRepository.updateBalance(addressId!!, token.shortName, balance)
                        }
                    }
                }.awaitAll()
        }

    fun loadTokensWithTotalBalance(listAddressWithTokens: List<AddressWithTokensLocal>) =
        viewModelScope.launch(ioDispatcher) {
            if (listAddressWithTokens.isEmpty()) return@launch

            val tokensWithBalance =
                TokenName.entries.map { token ->
                    val generalAddress =
                        listAddressWithTokens
                            .firstOrNull { address ->
                                address.address.isGeneralAddress &&
                                    address.tokens.any { it.tokenName == token.tokenName }
                            } ?: listAddressWithTokens.firstOrNull() // безопаснее, чем [1]

                    val totalBalance =
                        listAddressWithTokens
                            .flatMap { it.tokens }
                            .filter { it.tokenName == token.tokenName }
                            .sumOf { it.availableBalance }

                    TokenLocal(
                        addressId = generalAddress?.address?.id ?: 0,
                        tokenName = token.tokenName,
                        balance = totalBalance,
                    )
                }

            _tokensWithTotalBalance.emit(tokensWithBalance)
        }

    fun calculateTotalBalance(listTokensWithTotalBalance: List<TokenLocal>) =
        viewModelScope.launch(ioDispatcher) {
            if (listTokensWithTotalBalance.isEmpty()) {
                _totalBalance.value = BigInteger.ZERO
                return@launch
            }

            try {
                val trxToUsdtRate =
                    exchangeRatesLocalRepository.getRate(BinanceSymbol.TRX_USDT.symbol)

                val total =
                    listTokensWithTotalBalance.sumOf { token ->
                        if (token.tokenName == "TRX") {
                            val balanceInSun = token.balance.toTokenAmount()
                            val totalValue = balanceInSun.multiply(trxToUsdtRate.toBigDecimal())
                            totalValue.toSunAmount()
                        } else {
                            token.balance
                        }
                    }

                _totalBalance.value = total
            } catch (e: Exception) {
                _totalBalance.value = BigInteger.ZERO
                Log.e("WalletViewModel", "Failed to calculate total balance", e)
            }
        }

    fun calculateTotalPercentage24h(listTokensWithTotalBalance: List<TokenLocal>) =
        viewModelScope.launch(ioDispatcher) {
            if (listTokensWithTotalBalance.isEmpty()) {
                _totalPercentage24h.value = 0.0
                return@launch
            }

            try {
                val trxToUsdtRate =
                    exchangeRatesLocalRepository
                        .getRate(BinanceSymbol.TRX_USDT.symbol)
                        .toBigDecimal()

                val priceChangeUsdt =
                    tradingInsightsLocalRepository.getPriceChange24h(CoinSymbol.USDT_TRC20.id)
                val priceChangeTrx =
                    tradingInsightsLocalRepository.getPriceChange24h(CoinSymbol.TRON.id)

                val totalValue =
                    listTokensWithTotalBalance.sumOf { token ->
                        when (token.tokenName) {
                            "TRX" -> token.balance.toTokenAmount().multiply(trxToUsdtRate)
                            "USDT" -> token.balance.toBigDecimal()
                            else -> BigDecimal.ZERO
                        }
                    }

                val weightedSum =
                    listTokensWithTotalBalance.sumOf { token ->
                        when (token.tokenName) {
                            "TRX" ->
                                token.balance
                                    .toTokenAmount()
                                    .multiply(trxToUsdtRate)
                                    .multiply(priceChangeTrx.toBigDecimal())
                            "USDT" ->
                                token.balance
                                    .toBigDecimal()
                                    .multiply(priceChangeUsdt.toBigDecimal())
                            else -> BigDecimal.ZERO
                        }
                    }

                val result =
                    if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
                        BigDecimal.ZERO
                    } else {
                        weightedSum.divide(totalValue, 8, RoundingMode.HALF_UP)
                    }

                _totalPercentage24h.value = result.toDouble().coerceIn(-100.0, 100.0)
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Failed to calculate 24h percentage", e)
                _totalPercentage24h.value = 0.0
            }
        }
}
