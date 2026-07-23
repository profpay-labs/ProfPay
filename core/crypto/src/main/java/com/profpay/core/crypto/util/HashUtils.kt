package com.profpay.core.crypto.util

import java.security.MessageDigest

object HashUtils {

    fun sha256(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
    }

    fun sha256Hex(data: ByteArray): String {
        return ByteUtils.toHex(sha256(data))
    }
}
