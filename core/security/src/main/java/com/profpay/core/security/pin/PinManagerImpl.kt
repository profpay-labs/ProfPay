package com.profpay.core.security.pin

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.profpay.core.security.CryptoManager
import com.profpay.core.security.model.CipherData
import com.profpay.domain.security.repository.PinManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация менеджера PIN-кода.
 *
 * PIN шифруется через Android Keystore (CryptoManager) и хранится в DataStore.
 * При валидации расшифровываем и сравниваем через constant-time comparison.
 */
@Singleton
class PinManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: CryptoManager,
) : PinManager {

    override suspend fun hasPin(): Boolean =
        dataStore.data.map { it[PIN_KEY] }.first() != null

    override suspend fun savePin(pin: String) {
        val pinBytes = pin.toByteArray()
        val cipherData = cryptoManager.encrypt(PIN_ALIAS, pinBytes)
        val encoded = encodeForStorage(cipherData)

        dataStore.edit { prefs ->
            prefs[PIN_KEY] = encoded
        }

        // Очистка sensitive данных
        pinBytes.fill(0)
    }

    override suspend fun validatePin(pin: String): Boolean {
        val saved = dataStore.data.map { it[PIN_KEY] }.first() ?: return false

        return try {
            val cipherData = decodeFromStorage(saved)
            val decrypted = cryptoManager.decrypt(PIN_ALIAS, cipherData.iv, cipherData.cipherText)
            val pinBytes = pin.toByteArray()

            // Constant-time comparison для защиты от timing attacks
            val match = MessageDigest.isEqual(decrypted, pinBytes)

            // Очистка sensitive данных
            decrypted.fill(0)
            pinBytes.fill(0)

            match
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun clearPin() {
        dataStore.edit { prefs ->
            prefs.remove(PIN_KEY)
        }
        cryptoManager.deleteKey(PIN_ALIAS)
    }

    private fun encodeForStorage(cipherData: CipherData): String {
        // IV (12 bytes) + CipherText в Base64
        val combined = cipherData.iv + cipherData.cipherText
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decodeFromStorage(encoded: String): CipherData {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        return CipherData(
            iv = combined.copyOfRange(0, GCM_IV_SIZE),
            cipherText = combined.copyOfRange(GCM_IV_SIZE, combined.size),
        )
    }

    companion object {
        private val PIN_KEY = stringPreferencesKey("pin_encrypted")
        private const val PIN_ALIAS = "pin_encryption_key"
        private const val GCM_IV_SIZE = 12 // GCM IV = 12 bytes
    }
}
