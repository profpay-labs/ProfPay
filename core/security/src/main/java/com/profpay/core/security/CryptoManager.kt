package com.profpay.core.security

import com.profpay.core.security.model.CipherData

/**
 * Интерфейс для шифрования/дешифрования данных.
 *
 * Используется для безопасного хранения чувствительных данных
 * (entropy кошелька, seed phrase и т.д.)
 */
interface CryptoManager {

    /**
     * Шифрует данные с привязкой к alias (ключу в Keystore).
     *
     * @param alias Уникальный идентификатор ключа
     * @param plainText Данные для шифрования
     * @return Зашифрованные данные с IV
     */
    fun encrypt(alias: String, plainText: ByteArray): CipherData

    /**
     * Расшифровывает данные.
     *
     * @param alias Идентификатор ключа, использованного при шифровании
     * @param iv Вектор инициализации
     * @param cipherText Зашифрованные данные
     * @return Расшифрованные данные
     * @throws CryptoException при ошибке расшифровки
     */
    fun decrypt(alias: String, iv: ByteArray, cipherText: ByteArray): ByteArray

    /**
     * Проверяет наличие ключа в Keystore.
     */
    fun hasKey(alias: String): Boolean

    /**
     * Удаляет ключ из Keystore.
     */
    fun deleteKey(alias: String)
}
