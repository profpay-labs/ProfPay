package com.profpay.data.user.mapper

import com.profpay.core.database.entities.SettingsEntity
import com.profpay.domain.user.model.local.UserSettings

object SettingsMapper {

    fun SettingsEntity.toDomain(): UserSettings = UserSettings(
        id = id ?: 0L,
        languageCode = languageCode,
        isBotActive = activeBot,
        botToken = tokenBot.takeIf { it.isNotBlank() },
        isAutoAmlEnabled = autoAml,
    )

    fun UserSettings.toEntity(): SettingsEntity = SettingsEntity(
        id = if (id == 0L) null else id,
        languageCode = languageCode,
        activeBot = isBotActive,
        tokenBot = botToken ?: "",
        autoAml = isAutoAmlEnabled,
    )
}
