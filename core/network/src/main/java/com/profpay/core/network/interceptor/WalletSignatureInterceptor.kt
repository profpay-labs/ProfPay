package com.profpay.core.network.interceptor

import com.profpay.core.network.auth.WalletAuthProvider
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor для подписи запросов приватным ключом кошелька.
 *
 * Добавляет заголовки:
 * - X-Wallet-Address
 * - X-Wallet-Public-Key
 * - X-Wallet-Signature
 * - X-Wallet-Timestamp
 */
@Singleton
class WalletSignatureInterceptor @Inject constructor(
    private val walletAuthProvider: WalletAuthProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Пропускаем запросы без авторизации
        if (originalRequest.header(HEADER_NO_AUTH) != null) {
            return chain.proceed(
                originalRequest.newBuilder()
                    .removeHeader(HEADER_NO_AUTH)
                    .build()
            )
        }

        // Если нет кошелька — пропускаем
        if (!walletAuthProvider.hasWallet()) {
            return chain.proceed(originalRequest)
        }

        val walletAddress = walletAuthProvider.getWalletAddress()
        val publicKeyHex = walletAuthProvider.getPublicKeyHex()

        if (walletAddress.isNullOrBlank() || publicKeyHex.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val payload = buildPayload(originalRequest, timestamp)

        // Подписываем payload (синхронно, из кэша)
        val signature = walletAuthProvider.signPayload(payload)

        if (signature.isNullOrBlank()) {
            // Не удалось подписать — отправляем без авторизации
            return chain.proceed(originalRequest)
        }

        val signedRequest = originalRequest.newBuilder()
            .header(HEADER_WALLET_ADDRESS, walletAddress)
            .header(HEADER_WALLET_PUBLIC_KEY, publicKeyHex)
            .header(HEADER_WALLET_SIGNATURE, signature)
            .header(HEADER_WALLET_TIMESTAMP, timestamp)
            .build()

        return chain.proceed(signedRequest)
    }

    /**
     * Формирует payload для подписи:
     * METHOD\n
     * PATH\n
     * TIMESTAMP\n
     * SHA256(BODY)
     */
    private fun buildPayload(request: Request, timestamp: String): String {
        val method = request.method
        val path = request.url.encodedPath
        val bodyHash = request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            val bodyBytes = buffer.readByteArray()
            sha256Hex(bodyBytes)
        } ?: sha256Hex(ByteArray(0)) // Пустой body → SHA256 пустой строки

        return buildString {
            append(method)
            append('\n')
            append(path)
            append('\n')
            append(timestamp)
            append('\n')
            append(bodyHash)
        }
    }

    private fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val HEADER_NO_AUTH = "No-Authentication"
        private const val HEADER_WALLET_ADDRESS = "X-Wallet-Address"
        private const val HEADER_WALLET_PUBLIC_KEY = "X-Wallet-Public-Key"
        private const val HEADER_WALLET_SIGNATURE = "X-Wallet-Signature"
        private const val HEADER_WALLET_TIMESTAMP = "X-Wallet-Timestamp"
    }
}
