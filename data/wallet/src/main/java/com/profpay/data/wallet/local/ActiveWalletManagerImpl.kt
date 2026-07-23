package com.profpay.data.wallet.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.profpay.domain.wallet.ActiveWalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Имплементация менеджера активного кошелька.
 *
 * Хранит ID текущего выбранного кошелька в DataStore и предоставляет
 * реактивный доступ через StateFlow. При старте загружает значение
 * из персистентного хранилища.
 */
@Singleton
class ActiveWalletManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ActiveWalletManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * In-memory кэш для быстрого синхронного доступа.
     * Инициализируется из DataStore при создании.
     */
    private val _activeWalletIdFlow = MutableStateFlow(NO_WALLET_ID)
    override val activeWalletIdFlow: StateFlow<Long> = _activeWalletIdFlow.asStateFlow()

    override val activeWalletId: Long
        get() = _activeWalletIdFlow.value

    init {
        // Загружаем начальное значение синхронно (один раз при старте)
        runBlocking {
            _activeWalletIdFlow.value = loadFromDataStore()
        }

        // Подписываемся на изменения в DataStore
        observeDataStoreChanges()
    }

    override suspend fun setActiveWallet(walletId: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_WALLET_ID] = walletId
        }
        // StateFlow обновится автоматически через observeDataStoreChanges()
    }

    override fun hasActiveWallet(): Boolean =
        _activeWalletIdFlow.value != NO_WALLET_ID

    private suspend fun loadFromDataStore(): Long =
        dataStore.data
            .map { prefs -> prefs[KEY_ACTIVE_WALLET_ID] ?: DEFAULT_WALLET_ID }
            .first()

    private fun observeDataStoreChanges() {
        scope.launch {
            dataStore.data
                .map { prefs -> prefs[KEY_ACTIVE_WALLET_ID] ?: DEFAULT_WALLET_ID }
                .distinctUntilChanged()
                .collect { walletId ->
                    _activeWalletIdFlow.value = walletId
                }
        }
    }

    companion object {
        private val KEY_ACTIVE_WALLET_ID = longPreferencesKey("active_wallet_id")
        private const val DEFAULT_WALLET_ID = 1L
        private const val NO_WALLET_ID = -1L
    }
}
