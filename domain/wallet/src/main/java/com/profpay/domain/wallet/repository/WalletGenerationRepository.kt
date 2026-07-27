package com.profpay.domain.wallet.repository

import com.profpay.domain.wallet.model.GeneratedWalletData
import com.profpay.domain.wallet.model.RecoveryResult
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для генерации и восстановления кошельков.
 */
interface WalletGenerationRepository {
    /**
     * Flow с данными сгенерированного кошелька.
     */
    val generatedWallet: Flow<GeneratedWalletData>

    /**
     * Flow с результатом восстановления кошелька.
     */
    val recoveryResult: Flow<RecoveryResult>

    /**
     * Генерирует новый кошелёк с мнемоникой.
     */
    suspend fun generateNewWallet()

    /**
     * Восстанавливает кошелёк по мнемонике.
     */
    suspend fun recoverWalletFromMnemonic(mnemonic: String)

    /**
     * Очищает результат восстановления.
     */
    suspend fun clearRecoveryResult()
}
