package com.profpay.data.config.repository

import com.profpay.core.network.error.NetworkError
import com.profpay.core.network.error.safeApiCall
import com.profpay.data.config.api.ConfigApi
import com.profpay.data.config.dto.FeeConfigurationDto
import com.profpay.data.config.mapper.toDomain
import com.profpay.domain.config.exception.ConfigError
import com.profpay.domain.config.model.FeeConfiguration
import com.profpay.domain.config.repository.ConfigRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ConfigRepositoryImpl @Inject constructor(
    private val configApi: ConfigApi,
    private val json: Json,
) : ConfigRepository {

    override suspend fun getFeeConfiguration(): Result<FeeConfiguration> {
        val apiResult: Result<FeeConfigurationDto> = safeApiCall(json) {
            configApi.getFeeConfiguration()
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toConfigError() }
    }

    private fun Throwable.toConfigError(): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isServerError -> ConfigError.ServerError(this)
            else -> ConfigError.FetchFailed(this)
        }
        else -> ConfigError.FetchFailed(this)
    }
}
