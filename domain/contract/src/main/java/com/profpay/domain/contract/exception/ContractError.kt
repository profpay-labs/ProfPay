package com.profpay.domain.contract.exception

sealed class ContractError : Exception() {

    data class UserNotFound(
        val userId: Long,
    ) : ContractError() {
        override val message: String = "User not found: $userId"
    }

    data class ContractUnavailable(
        override val cause: Throwable? = null,
    ) : ContractError() {
        override val message: String = "Contract service is temporarily unavailable"
    }

    /**
     * Некорректные данные для деплоя или недостаточная комиссия
     */
    data class InvalidDeployRequest(
        val reason: String,
    ) : ContractError() {
        override val message: String = "Invalid deploy request: $reason"
    }

    /**
     * Недостаточно ресурсов для деплоя
     */
    data class InsufficientResources(
        val required: Long,
        val available: Long,
    ) : ContractError() {
        override val message: String = "Insufficient resources: required $required, available $available"
    }

    /**
     * Смарт-контракт покупателя не найден
     */
    data class BuyerContractNotFound(
        val buyerUserId: Long,
    ) : ContractError() {
        override val message: String = "Smart contract not found for buyer: $buyerUserId"
    }

    /**
     * Недостаточно арбитров в группе
     */
    data class InsufficientArbiters(
        val groupId: Long,
        val required: Int = 3,
    ) : ContractError() {
        override val message: String = "Insufficient arbiters in group $groupId: need at least $required"
    }

    /**
     * Недостаточная комиссия или некорректные данные для вызова контракта
     */
    data class InvalidCallRequest(
        val reason: String,
    ) : ContractError() {
        override val message: String = "Invalid contract call request: $reason"
    }

    /**
     * Контракт не найден
     */
    data class ContractNotFound(
        val contractAddress: String,
    ) : ContractError() {
        override val message: String = "Contract not found: $contractAddress"
    }

    /** Некорректное действие для диспута */
    data class InvalidDisputeAction(
        val action: String,
        override val message: String = "Invalid dispute action: $action",
    ) : ContractError()

    /** Сделка не найдена */
    data class DealNotFound(
        val dealId: Long,
        override val message: String = "Deal not found: $dealId",
    ) : ContractError()

    /** Пользователь не является участником диспута */
    data class NotDisputeParticipant(
        val userId: Long,
        val dealId: Long,
        override val message: String = "User $userId is not a participant of deal $dealId",
    ) : ContractError()
}
