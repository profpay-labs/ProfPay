package com.profpay.core.crypto.model

import com.profpay.core.crypto.util.ByteUtils

/**
 * ECDSA подпись (R + S компоненты).
 */
data class Signature(
    val r: ByteArray,
    val s: ByteArray,
) {
    /**
     * Возвращает подпись в hex-формате (64 байта).
     */
    fun toHex(): String = ByteUtils.toHex(r) + ByteUtils.toHex(s)

    /**
     * Возвращает подпись как ByteArray (64 байта).
     */
    fun toByteArray(): ByteArray = r + s

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Signature
        return r.contentEquals(other.r) && s.contentEquals(other.s)
    }

    override fun hashCode(): Int = 31 * r.contentHashCode() + s.contentHashCode()
}
