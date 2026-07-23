package com.profpay.core.network.auth

/**
 * Провайдер данных кошелька для подписи HTTP-запросов.
 *
 * Все методы СИНХРОННЫЕ и возвращают данные из in-memory кэша.
 * Кэш обновляется реактивно при изменениях в БД.
 */
interface WalletAuthProvider {

    /**
     * TRON адрес текущего кошелька (например, TXyz...)
     * @return адрес или null если кошелёк не настроен
     */
    fun getWalletAddress(): String?

    /**
     * Публичный ключ в hex формате
     * @return публичный ключ или null если кошелёк не настроен
     */
    fun getPublicKeyHex(): String?

    /**
     * Подписывает payload приватным ключом.
     *
     * ВАЖНО: Метод синхронный, криптография выполняется inline.
     *
     * @param payload — строка для подписи (METHOD\nPATH\nTIMESTAMP\nSHA256(BODY))
     * @return ECDSA подпись в hex формате или null при ошибке
     */
    fun signPayload(payload: String): String?

    /**
     * Есть ли активный кошелёк с закэшированными данными
     */
    fun hasWallet(): Boolean

    /**
     * Очищает кэшированный приватный ключ из памяти.
     * Вызывается при уходе приложения в background для безопасности.
     */
    fun clearPrivateKeyCache()
}
