package com.profpay.core.tron.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TronNode(
    val grpc: String,
    val solidityGrpc: String,
)

/**
 * Менеджер TRON нод с failover логикой.
 * При ошибке переключается на следующую ноду.
 */
object TronNodeManager {

    private val nodes = listOf(
        TronNode("45.137.213.192:59151", "45.137.213.192:50061"),
        // резервные ноды при необходимости
    )

    private var currentIndex = 0

    fun <T> executeWithFailover(block: (TronNode) -> T): T {
        var lastException: Exception? = null

        repeat(nodes.size) { attempt ->
            val node = nodes[(currentIndex + attempt) % nodes.size]
            try {
                val result = block(node)
                currentIndex = (currentIndex + attempt) % nodes.size
                return result
            } catch (e: Exception) {
                Log.w(TAG, "Node ${node.grpc} failed: ${e.message}")
                lastException = e
            }
        }

        throw lastException ?: RuntimeException("All TRON nodes failed")
    }

    suspend fun <T> executeWithFailoverSuspend(block: suspend (TronNode) -> T): T {
        return withContext(Dispatchers.IO) {
            var lastException: Exception? = null

            repeat(nodes.size) { attempt ->
                val node = nodes[(currentIndex + attempt) % nodes.size]
                try {
                    val result = block(node)
                    currentIndex = (currentIndex + attempt) % nodes.size
                    return@withContext result
                } catch (e: Exception) {
                    Log.w(TAG, "Node ${node.grpc} failed: ${e.message}")
                    lastException = e
                }
            }

            throw lastException ?: RuntimeException("All TRON nodes failed")
        }
    }

    private const val TAG = "TronNodeManager"
}
