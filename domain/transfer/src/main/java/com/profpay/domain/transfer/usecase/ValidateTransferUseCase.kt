package com.profpay.domain.transfer.usecase

import com.profpay.domain.transfer.exception.TransferError
import com.profpay.domain.transfer.repository.BalanceChecker
import com.profpay.domain.wallet.model.TokenType
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Валидация условий перевода перед подписанием транзакции.
 *
 * Чистая бизнес-логика без side-effects. Проверяет:
 * - Активацию адреса
 * - Достаточность баланса для комиссии
 * - Достаточность баланса токенов
 * - Для TRX: баланс после вычета комиссии
 */
class ValidateTransferUseCase @Inject constructor(
    private val balanceChecker: BalanceChecker,
) {

    suspend fun validate(params: Params): Result<Unit> {
        // 1. Активация адреса отправителя
        if (!balanceChecker.isAddressActivated(params.senderAddress)) {
            return Result.failure(TransferError.AddressNotActivated)
        }

        // 2. Баланс для комиссии (TRX платит с sender, остальные — с commission address)
        val feeAddressForCheck = if (params.tokenType == TokenType.TRX) {
            params.senderAddress
        } else {
            params.commissionAddress
        }

        val feeBalance = balanceChecker.getTrxBalance(feeAddressForCheck)
        val commissionDecimal = params.commission

        if (feeBalance < commissionDecimal) {
            return Result.failure(
                TransferError.InsufficientFeeBalance(
                    feeAddress = feeAddressForCheck,
                    required = commissionDecimal.toPlainString(),
                    available = feeBalance.toPlainString(),
                ),
            )
        }

        // 3. Достаточно ли токенов
        val balanceDecimal = params.tokenBalance
        val amountDecimal = params.amount

        if (balanceDecimal < amountDecimal) {
            return Result.failure(
                TransferError.AmountExceedsBalance(
                    requested = amountDecimal.toPlainString(),
                    available = balanceDecimal.toPlainString(),
                ),
            )
        }

        // 4. Для TRX: хватит ли после вычета комиссии
        if (params.tokenType == TokenType.TRX) {
            val remaining = balanceDecimal - amountDecimal - commissionDecimal
            if (remaining < BigDecimal.ZERO) {
                return Result.failure(
                    TransferError.InsufficientBalanceWithCommission(
                        remaining = remaining.toPlainString(),
                    ),
                )
            }
        }

        // 5. Комиссия > 0
        if (commissionDecimal <= BigDecimal.ZERO) {
            return Result.failure(TransferError.InvalidCommission)
        }

        return Result.success(Unit)
    }

    data class Params(
        val senderAddress: String,
        val commissionAddress: String,
        val tokenType: TokenType,
        val tokenBalance: BigDecimal,  // уже в token units, не в SUN
        val amount: BigDecimal,        // уже в token units, не в SUN
        val commission: BigDecimal,    // уже в TRX, не в SUN
    )
}
