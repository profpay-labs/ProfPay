package com.profpay.core.crypto.util

object ByteUtils {

    fun toHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    fun parseHex(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hex string must have even length" }
        return hex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}
