package com.profpay.data.contract.repository

import android.util.Log
import com.profpay.domain.contract.model.Deal
import com.profpay.domain.contract.repository.ContractRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory кэш списка сделок юзера.
 *
 * Подписчики получают актуальный список через [deals] flow.
 * Обновление происходит при вызове [refresh] — обычно из ViewModel или push-обработчиков.
 */
@Singleton
class DealsStore @Inject constructor(
    private val contractRepository: ContractRepository,
    private val profileLocalRepository: ProfileLocalRepository,
) {
    private val _deals = MutableStateFlow<List<Deal>>(emptyList())
    val deals: StateFlow<List<Deal>> = _deals.asStateFlow()

    /**
     * Загружает актуальный список сделок с сервера.
     * При ошибке логируем в Sentry, но не крашим — список просто останется прежним.
     */
    suspend fun refresh(): Result<Unit> =
        contractRepository.getUserDeals(profileLocalRepository.getUserId())
            .onSuccess { _deals.value = it.deals }
            .onFailure { it.message?.let { msg -> Log.e("DealsStore", msg) } }
            .map { }
}
