package com.profpay.domain.wallet.repository

import com.profpay.domain.wallet.model.AddWalletParams
import com.profpay.domain.wallet.model.CentralAddressParams
import com.profpay.domain.wallet.model.CentralAddressResult
import com.profpay.domain.wallet.model.CreateWalletParams
import com.profpay.domain.wallet.model.UpdateDerivedIndexParams
import com.profpay.domain.wallet.model.UpdateDerivedIndexResult
import com.profpay.domain.wallet.model.WalletDataResult
import com.profpay.domain.wallet.model.WalletResult

interface WalletRepository {

    /**
     * Обновить derived index SOT-адреса
     */
    suspend fun updateDerivedIndex(params: UpdateDerivedIndexParams): Result<UpdateDerivedIndexResult>

    /**
     * Получить данные кошелька по адресу
     */
    suspend fun getWalletData(address: String): Result<WalletDataResult>

    /**
     * Установить центральный адрес
     */
    suspend fun setCentralAddress(params: CentralAddressParams): Result<CentralAddressResult>

    /**
     * Добавить дополнительный кошелёк к существующему пользователю.
     * Центральный адрес не создаётся (он един на все кошельки).
     */
    suspend fun addWallet(params: AddWalletParams): Result<WalletResult>
}
