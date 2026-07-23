package com.profpay.domain.wallet.repository

/**
 * Репозиторий для перевыпуска центрального адреса.
 */
interface ReissueCentralAddressRepository {
    /**
     * Генерирует новый центральный адрес и отправляет на сервер.
     * @return Result.success(Unit) при успехе, Result.failure с ошибкой при неудаче
     */
    suspend fun changeCentralAddress(): Result<Unit>
}
