package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response проверки существования пользователя
 */
@Serializable
data class UserExistsResponseDto(
    @SerialName("exists")
    val exists: Boolean,
)
