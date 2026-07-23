package com.profpay.wallet.bridge.viewmodel.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.common.di.MainDispatcher
import com.profpay.domain.user.repository.UserRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SettingsAccountViewModel
    @Inject
    constructor(
        private val profileRepo: ProfileLocalRepository,
        private val userRepository: UserRepository,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        val profileTelegramId: LiveData<Long?> =
            liveData(ioDispatcher) {
                emitSource(profileRepo.observeTelegramId().asLiveData())
            }

        val profileTelegramUsername: LiveData<String?> =
            liveData(ioDispatcher) {
                emitSource(profileRepo.observeTelegramUsername().asLiveData())
            }

        fun loadUserAndAppIds(
            onLoaded: (userId: Long, appId: String) -> Unit,
            onError: (Throwable) -> Unit = {},
        ) = viewModelScope.launch(ioDispatcher) {
            try {
                val userId = profileRepo.getUserId()
                val appId = profileRepo.getAppId()
                withContext(mainDispatcher) {
                    onLoaded(userId, appId)
                }
            } catch (e: Exception) {
                onError(e)
            }
        }

    fun getUserTelegramData() =
        viewModelScope.launch(ioDispatcher) {
            val telegramData = userRepository.getTelegramData(profileRepo.getAppId())

            telegramData.fold(
                onSuccess = {
                    profileRepo.updateTelegram(
                        telegramId = it.telegramId,
                        username = it.username,
                    )
                },
                onFailure = { throwable ->
                    if (throwable is CancellationException) {
                        throw throwable
                    }
                    Sentry.captureException(throwable)
                },
            )
        }
    }
