package com.profpay.data.user.mapper

import com.profpay.core.database.entities.ProfileEntity
import com.profpay.domain.user.model.local.UserProfile

object ProfileMapper {

    fun ProfileEntity.toDomain(): UserProfile = UserProfile(
        userId = userId,
        appId = appId,
        telegramId = telegramId,
        telegramUsername = username?.takeIf { it.isNotBlank() },
        deviceToken = deviceToken?.takeIf { it.isNotBlank() },
        isActive = activeTgId,
    )

    fun UserProfile.toEntity(): ProfileEntity = ProfileEntity(
        userId = userId,
        appId = appId,
        telegramId = telegramId,
        username = telegramUsername,
        deviceToken = deviceToken,
        activeTgId = isActive,
        accessToken = null,
        expiresAt = null,
    )
}
