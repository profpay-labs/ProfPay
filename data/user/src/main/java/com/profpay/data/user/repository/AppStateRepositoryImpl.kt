package com.profpay.data.user.repository

import com.profpay.core.database.dao.ProfileDao
import com.profpay.core.database.dao.wallet.WalletProfileDao
import com.profpay.domain.user.model.AppState
import com.profpay.domain.user.repository.AppStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val walletProfileDao: WalletProfileDao,
) : AppStateRepository {

    override suspend fun getAppState(): AppState {
        val profile = profileDao.getProfile()

        // Профиль существует И userId не null — считаем зарегистрированным
        return if (profile != null && profile.userId != null && profile.appId != null) {
            val walletsCount = walletProfileDao.getWalletsCount()
            AppState.Registered(
                userId = profile.userId!!,
                appId = profile.appId!!,
                walletsCount = walletsCount,
            )
        } else {
            AppState.NotRegistered
        }
    }

    override fun observeAppState(): Flow<AppState> {
        return combine(
            profileDao.observeProfile(),
            walletProfileDao.observeWalletsCount(),
        ) { profile, walletsCount ->
            if (profile != null && profile.userId != null && profile.appId != null) {
                AppState.Registered(
                    userId = profile.userId!!,
                    appId = profile.appId!!,
                    walletsCount = walletsCount,
                )
            } else {
                AppState.NotRegistered
            }
        }
    }
}
