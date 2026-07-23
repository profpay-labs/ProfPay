package com.profpay.data.config.api

import com.profpay.data.config.dto.FeeConfigurationDto
import retrofit2.Response
import retrofit2.http.GET

interface ConfigApi {

    @GET("system/fee-configuration")
    suspend fun getFeeConfiguration(): Response<FeeConfigurationDto>
}
