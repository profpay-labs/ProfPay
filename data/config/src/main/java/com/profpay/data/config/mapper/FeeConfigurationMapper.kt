package com.profpay.data.config.mapper

import com.profpay.data.config.dto.FeeConfigurationDto
import com.profpay.domain.config.model.FeeConfiguration

fun FeeConfigurationDto.toDomain(): FeeConfiguration = FeeConfiguration(
    trxFeeAddress = trxFeeAddress,
    amlFee = amlFee,
    timestamp = timestamp,
)
