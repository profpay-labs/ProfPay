package com.profpay.data.user.repository.local

import com.profpay.core.database.dao.ProfileDao
import com.profpay.data.user.mapper.ProfileMapper.toEntity
import com.profpay.domain.user.model.local.UserProfile
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
) : ProfileLocalRepository {

    // ══════════════════════════════════════════════════════════════════════
    // Основные идентификаторы
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun getUserId(): Long = profileDao.getProfileUserId()

    override suspend fun getAppId(): String = profileDao.getProfileAppId()

    // ══════════════════════════════════════════════════════════════════════
    // CRUD операции
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun create(profile: UserProfile) {
        profileDao.insertNewProfile(profile.toEntity())
    }

    override suspend fun exists(): Boolean = profileDao.isProfileExists()

    override suspend fun deleteByTelegramId(telegramId: Long) {
        profileDao.deleteProfileByTgId(telegramId)
    }

    // ══════════════════════════════════════════════════════════════════════
    // Telegram интеграция
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun getTelegramId(): Long? = profileDao.getProfileTelegramId()

    override fun observeTelegramId(): Flow<Long?> = profileDao.getProfileTelegramIdFlow()

    override fun observeTelegramUsername(): Flow<String?> = profileDao.getProfileTgUsernameFlow()

    override suspend fun updateTelegram(telegramId: Long, username: String) {
        profileDao.updateProfileTelegramIdAndUsername(telegramId, username)
    }

    override suspend fun updateTelegramActivation(
        isActive: Boolean,
        telegramId: Long,
        accessToken: String,
        expiresAt: Long,
    ) {
        profileDao.updateActiveTgId(isActive, telegramId, accessToken, expiresAt)
    }

    // ══════════════════════════════════════════════════════════════════════
    // Device & Server sync
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun getDeviceToken(): String? = profileDao.getDeviceToken()

    override suspend fun updateDeviceToken(deviceToken: String) {
        profileDao.updateDeviceToken(deviceToken)
    }

    override suspend fun updateUserId(userId: Long) {
        profileDao.updateUserId(userId)
    }
}
