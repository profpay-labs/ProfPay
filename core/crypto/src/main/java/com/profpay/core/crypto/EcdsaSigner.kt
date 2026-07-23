package com.profpay.core.crypto

import com.profpay.core.crypto.model.Signature
import com.profpay.core.crypto.util.HashUtils
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.jce.ECNamedCurveTable
import java.math.BigInteger

/**
 * ECDSA подпись на кривой secp256k1.
 *
 * Используется для подписи транзакций и HTTP-запросов.
 */
object EcdsaSigner {

    private val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
    private val domainParams = ECDomainParameters(
        ecParams.curve, ecParams.g, ecParams.n, ecParams.h
    )

    /**
     * Подписывает данные приватным ключом.
     *
     * @param data Данные для подписи
     * @param privateKeyHex Приватный ключ в hex
     * @return Подпись с Low-S нормализацией
     */
    fun sign(data: ByteArray, privateKeyHex: String): Signature {
        val hash = HashUtils.sha256(data)
        return signHash(hash, privateKeyHex)
    }

    /**
     * Подписывает уже вычисленный хеш.
     *
     * @param hash SHA256 хеш (32 байта)
     * @param privateKeyHex Приватный ключ в hex
     * @return Подпись (R + S по 32 байта)
     */
    fun signHash(hash: ByteArray, privateKeyHex: String): Signature {
        val privateKeyBigInt = BigInteger(privateKeyHex, 16)
        val privateKeyParams = ECPrivateKeyParameters(privateKeyBigInt, domainParams)

        val signer = ECDSASigner()
        signer.init(true, privateKeyParams)
        val components = signer.generateSignature(hash)

        val r = components[0]
        var s = components[1]

        // Low-S нормализация
        val halfN = ecParams.n.shiftRight(1)
        if (s > halfN) {
            s = ecParams.n.subtract(s)
        }

        return Signature(
            r = bigIntTo32Bytes(r),
            s = bigIntTo32Bytes(s),
        )
    }

    private fun bigIntTo32Bytes(value: BigInteger): ByteArray {
        val bytes = value.toByteArray()
        return when {
            bytes.size == 32 -> bytes
            bytes.size > 32 -> bytes.takeLast(32).toByteArray()
            else -> ByteArray(32 - bytes.size) + bytes
        }
    }
}
