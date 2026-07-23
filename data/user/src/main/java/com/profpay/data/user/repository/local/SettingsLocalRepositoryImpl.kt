package com.profpay.data.user.repository.local

import com.profpay.core.database.dao.SettingsDao
import com.profpay.core.database.entities.SettingsEntity
import com.profpay.data.user.mapper.SettingsMapper.toDomain
import com.profpay.domain.user.model.local.UserSettings
import com.profpay.domain.user.repository.local.SettingsLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsLocalRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
) : SettingsLocalRepository {

    override suspend fun createDefaultIfNotExists(languageCode: String) {
        if (!settingsDao.isSettingsExists()) {
            settingsDao.insertNewSettings(
                SettingsEntity(
                    languageCode = languageCode,
                    activeBot = false,
                    tokenBot = "",
                    autoAml = false,
                )
            )
        }
    }

    override suspend fun get(): UserSettings? {
        return settingsDao.getSettings()?.toDomain()
    }

    override fun observe(): Flow<UserSettings> {
        return settingsDao.getSettingsFlow().map { it.toDomain() }
    }

    override suspend fun getLanguageCode(): String {
        return settingsDao.getLanguageCode()
    }

    override suspend fun updateBotActive(isActive: Boolean) {
        settingsDao.updateActiveBot(isActive)
    }

    override suspend fun updateBotToken(token: String) {
        settingsDao.updateBotToken(token)
    }

    override suspend fun updateAutoAml(isEnabled: Boolean) {
        settingsDao.updateAutoAML(isEnabled)
    }

    override suspend fun exists(): Boolean {
        return settingsDao.isSettingsExists()
    }
}
