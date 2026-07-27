package com.profpay.domain.wallet.repository

import com.profpay.domain.wallet.model.WalletAddressesData

/**
 * Репозиторий для создания кошельков.
 * Чистый интерфейс без Android-зависимостей.
 */
interface WalletCreationRepository {
    /**
     * Возвращает главный адрес кошелька (derivation index = 0).
     */
    fun getWalletAlias(addressesData: WalletAddressesData): String?

    /**
     * Сохраняет адреса кошелька в локальное хранилище.
     * @return ID созданного кошелька
     */
    suspend fun insertNewCryptoAddresses(addressesData: WalletAddressesData): Long

    /**
     * Регистрирует устройство пользователя на сервере.
     * @param deviceToken Токен устройства для push-уведомлений
     */
    suspend fun registerUserDevice(
        userId: Long,
        deviceToken: String,
    )
}
