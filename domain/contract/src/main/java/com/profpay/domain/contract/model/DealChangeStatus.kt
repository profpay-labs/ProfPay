package com.profpay.domain.contract.model

enum class DealChangeStatus {
    BUYER_CREATED,
    BUYER_DEPOSITED,
    BUYER_CONFIRMED,
    SELLER_CONFIRMED,
    SELLER_PAID_EXPERT_FEE,
    BUYER_CANCEL_CONTRACT,
    BUYER_CANCEL_PAID_CONTRACT,
    SELLER_CANCEL_CONTRACT,
    BUYER_CANCEL_DEAL,
    SELLER_CANCEL_DEAL,
    BUYER_OPEN_DISPUTE,
    SELLER_OPEN_DISPUTE,
    BUYER_DELETE_CONTRACT,
    EXPERT_SET_DECISION,
    EXPERT_DISPUTE_AGREED,
    SELLER_DISPUTE_AGREED,
    BUYER_DISPUTE_AGREED,
    EXPERT_DISPUTE_DECLINE,
    SELLER_DISPUTE_DECLINE,
    BUYER_DISPUTE_DECLINE;

    companion object {
        fun fromString(value: String): DealChangeStatus =
            entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown status: $value")
    }
}
