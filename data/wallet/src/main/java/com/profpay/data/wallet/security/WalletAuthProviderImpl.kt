package com.profpay.data.wallet.security

import com.profpay.core.crypto.EcdsaSigner
import com.profpay.core.network.auth.WalletAuthProvider
import com.profpay.domain.security.PrivateKeyProvider
import com.profpay.domain.wallet.ActiveWalletManager
import com.profpay.domain.wallet.model.local.WalletAddressLocal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация [WalletAuthProvider] с in-memory кэшированием данных кошелька.
 *
 * ## Архитектура
 * - Данные кошелька (адрес, публичный ключ) кэшируются в памяти
 * - Кэш обновляется реактивно через Flow при изменениях в Room
 * - Приватный ключ расшифровывается при первом `signPayload()` и кэшируется
 * - Все публичные методы синхронные — безопасно использовать в OkHttp Interceptor
 *
 * ## Lifecycle
 * - Singleton, живёт весь lifecycle приложения
 * - Подписка на изменения кошелька через [CoroutineScope] с SupervisorJob
 *
 * @property addressLocalRepository Репозиторий для доступа к адресам кошелька
 * @property activeWalletManager Менеджер активного кошелька для получения ID
 * @property privateKeyProvider Провайдер для расшифровки приватного ключа
 */
@Singleton
class WalletAuthProviderImpl @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val activeWalletManager: ActiveWalletManager,
    private val privateKeyProvider: PrivateKeyProvider,
) : WalletAuthProvider {

    /**
     * Scope для фоновых операций (подписка на Flow).
     * SupervisorJob гарантирует, что ошибка в одной корутине не убьёт остальные.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Кэшированные данные текущего кошелька.
     * AtomicReference для thread-safe доступа из OkHttp потоков.
     */
    private val cachedWalletData = AtomicReference<WalletAuthData?>(null)

    init {
        observeWalletChanges()
    }

    // ══════════════════════════════════════════════════════════════════════
    // Public API (все методы синхронные!)
    // ══════════════════════════════════════════════════════════════════════

    override fun getWalletAddress(): String? = cachedWalletData.get()?.address

    override fun getPublicKeyHex(): String? = cachedWalletData.get()?.publicKeyHex

    override fun hasWallet(): Boolean = cachedWalletData.get() != null

    override fun signPayload(payload: String): String? {
        val walletData = cachedWalletData.get() ?: return null

        return try {
            // Получаем или расшифровываем приватный ключ
            val privateKeyHex = walletData.getOrResolvePrivateKey {
                resolvePrivateKey(walletData.addressEntity)
            } ?: return null

            val signature = EcdsaSigner.sign(payload.toByteArray(), privateKeyHex)
            signature.toHex()
        } catch (e: Exception) {
            // В production можно логировать в Sentry без sensitive данных
            null
        }
    }

    override fun clearPrivateKeyCache() {
        cachedWalletData.get()?.clearPrivateKey()
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private: подписки и кэширование
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Подписывается на изменения general address текущего кошелька.
     */
    private fun observeWalletChanges() {
        scope.launch {
            activeWalletManager.activeWalletIdFlow
                .collectLatest { walletId ->
                    val walletData = loadWalletData(walletId)
                    cachedWalletData.set(walletData)
                }
        }
    }

    /**
     * Загружает данные кошелька из БД.
     */
    private suspend fun loadWalletData(walletId: Long): WalletAuthData? {
        return try {
            val addressEntity = addressLocalRepository.getGeneralAddressEntityByWalletId(walletId)
            WalletAuthData(
                address = addressEntity.address,
                publicKeyHex = addressEntity.publicKey,
                addressEntity = addressEntity,
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Расшифровывает приватный ключ из Keystore.
     * Вызывается один раз, результат кэшируется в [WalletAuthData].
     */
    private fun resolvePrivateKey(addressEntity: WalletAddressLocal): String? {
        return try {
            // runBlocking здесь допустим: вызывается один раз при первой подписи,
            // результат кэшируется. PrivateKeyResolver — это CPU-bound операция.
            runBlocking(Dispatchers.Default) {
                privateKeyProvider.resolveHex(addressEntity)
            }
        } catch (e: Exception) {
            null
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Data classes
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Кэшированные данные кошелька для подписи.
     *
     * @property privateKeyHex Приватный ключ (lazy, кэшируется после первого resolve)
     */
    private class WalletAuthData(
        val address: String,
        val publicKeyHex: String,
        val addressEntity: WalletAddressLocal,
    ) {
        @Volatile
        private var privateKeyHex: String? = null

        /**
         * Получает приватный ключ из кэша или вызывает [resolver] для расшифровки.
         * Thread-safe благодаря double-checked locking.
         */
        fun getOrResolvePrivateKey(resolver: () -> String?): String? {
            privateKeyHex?.let { return it }

            synchronized(this) {
                privateKeyHex?.let { return it }
                privateKeyHex = resolver()
                return privateKeyHex
            }
        }

        fun clearPrivateKey() {
            synchronized(this) {
                privateKeyHex = null
            }
        }
    }
}
