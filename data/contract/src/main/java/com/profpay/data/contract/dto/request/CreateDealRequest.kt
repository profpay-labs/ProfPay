package com.profpay.data.contract.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request для создания сделки
 */
@Serializable
data class CreateDealRequest(
    @SerialName("buyerUserId")
    val buyerUserId: Long,
    @SerialName("sellerUserId")
    val sellerUserId: Long,
    @SerialName("arbiterGroupId")
    val arbiterGroupId: Long,
    @SerialName("amount")
    val amount: Long,
)
