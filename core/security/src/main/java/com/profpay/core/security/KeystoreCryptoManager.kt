package com.profpay.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.profpay.core.security.exception.CryptoException
import com.profpay.core.security.model.CipherData
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация [CryptoManager] на основе Android Keystore.
 *
 * Использует AES-256-GCM для шифрования.
 * Ключи хранятся в аппаратном Keystore и не могут быть извлечены.
 */
@Singleton
class KeystoreCryptoManager @Inject constructor() : CryptoManager {

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    override fun encrypt(alias: String, plainText: ByteArray): CipherData {
        val secretKey = getOrCreateKey(alias)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val cipherText = cipher.doFinal(plainText)
        return CipherData(
            iv = cipher.iv,
            cipherText = cipherText,
        )
    }

    override fun decrypt(alias: String, iv: ByteArray, cipherText: ByteArray): ByteArray {
        val secretKey = getKey(alias)
            ?: throw CryptoException.KeyNotFound(alias)

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            cipher.doFinal(cipherText)
        } catch (e: Exception) {
            throw CryptoException.DecryptionFailed(alias, e)
        }
    }

    override fun hasKey(alias: String): Boolean {
        return keyStore.containsAlias(alias)
    }

    override fun deleteKey(alias: String) {
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    private fun getOrCreateKey(alias: String): SecretKey {
        return getKey(alias) ?: createKey(alias)
    }

    private fun getKey(alias: String): SecretKey? {
        return keyStore.getKey(alias, null) as? SecretKey
    }

    private fun createKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val keyGenSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
    }
}
