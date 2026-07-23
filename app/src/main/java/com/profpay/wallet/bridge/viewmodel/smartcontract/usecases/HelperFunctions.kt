package com.profpay.wallet.bridge.viewmodel.smartcontract.usecases

import com.profpay.domain.contract.model.Deal

private const val TRON_ADDRESS_ZERO = "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb"

fun isBuyerRequestInitialized(
    deal: Deal,
    userId: Long,
): Boolean = deal.buyer.userId == userId && deal.blockchainDealId == 0L

fun isBuyerNotDeposited(
    deal: Deal,
    userId: Long,
): Boolean = deal.buyer.userId == userId && deal.blockchainDealId != 0L && !deal.blockchainData!!.paymentStatus.buyerDepositAndExpertFeePaid

fun isSellerNotPayedExpertFee(
    deal: Deal,
    userId: Long,
): Boolean =
    deal.seller.userId == userId &&
        deal.blockchainDealId != 0L &&
        !deal.blockchainData!!.paymentStatus.sellerExpertFeePaid &&
        deal.blockchainData!!.paymentStatus.buyerDepositAndExpertFeePaid &&
        !deal.blockchainData!!.agreementStatus.disputed

fun isContractAwaitingUserConfirmation(
    deal: Deal,
    userId: Long,
): Boolean {
    val isBuyer = deal.buyer.userId == userId
    val isSeller = deal.seller.userId == userId
    val buyerNotAgreed = !deal.blockchainData!!.agreementStatus.buyerAgreed
    val sellerNotAgreed = !deal.blockchainData!!.agreementStatus.sellerAgreed
    val sellerFeePaid = deal.blockchainData!!.paymentStatus.sellerExpertFeePaid
    val buyerFeePaid = deal.blockchainData!!.paymentStatus.buyerDepositAndExpertFeePaid

    val isAwaitingBuyerConfirmation = isBuyer && buyerNotAgreed
    val isAwaitingSellerConfirmation = isSeller && sellerNotAgreed

    return ((isAwaitingBuyerConfirmation || isAwaitingSellerConfirmation) && sellerFeePaid && buyerFeePaid) &&
        !deal.blockchainData!!.agreementStatus.disputed
}

fun isAddressZero(address: String): Boolean = address == TRON_ADDRESS_ZERO

fun isExpertNotDecision(
    deal: Deal,
    userId: Long,
): Boolean =
    deal.blockchainDealId != 0L &&
        deal.blockchainData!!.agreementStatus.disputed &&
        isAddressZero(deal.disputeStatus!!.decisionAdmin) &&
        isUserExpertComparison(deal, userId)

fun isDisputeNotAgreed(
    deal: Deal,
    userId: Long,
): Boolean =
    deal.blockchainDealId != 0L &&
        deal.blockchainData!!.agreementStatus.disputed &&
        !isAddressZero(deal.disputeStatus!!.decisionAdmin) &&
        (
            isUserExpertComparison(deal, userId) &&
                isExpertNotAgreedComparison(deal, userId)
        ) ||
        deal.blockchainData!!.agreementStatus.disputed &&
        (
            deal.seller.userId == userId &&
                !deal.disputeStatus!!.sellerAgreed ||
                deal.buyer.userId == userId &&
                !deal.disputeStatus!!.buyerAgreed
        )

fun isDisputeNotDeclined(
    deal: Deal,
    userId: Long,
): Boolean =
    deal.blockchainDealId != 0L &&
        deal.blockchainData!!.agreementStatus.disputed &&
        !isAddressZero(deal.disputeStatus!!.decisionAdmin) &&
        (
            isUserExpertComparison(deal, userId) &&
                isExpertNotDeclinedComparison(deal, userId)
        ) ||
        deal.blockchainData!!.agreementStatus.disputed &&
        (
            deal.seller.userId == userId &&
                !deal.disputeStatus!!.sellerDeclined ||
                deal.buyer.userId == userId &&
                !deal.disputeStatus!!.buyerDeclined
        )

private fun isExpertNotDeclinedComparison(
    deal: Deal,
    userId: Long,
): Boolean {
    // Найти администратора с соответствующим userId
    val admin = deal.admins.find { it.userId == userId }

    // Если администратор найден и его адрес есть в списке отклоненных, вернуть false, иначе true
    return admin?.let {
        deal.disputeStatus!!.adminsDeclined.none { it == admin.walletAddress }
    } ?: true
}

private fun isExpertNotAgreedComparison(
    deal: Deal,
    userId: Long,
): Boolean {
    // Найти администратора с соответствующим userId
    val admin = deal.admins.find { it.userId == userId }

    // Если администратор найден и его адрес есть в списке согласованных, вернуть false, иначе true
    return admin?.let {
        deal.disputeStatus!!.adminsAgreed.none { it == admin.walletAddress }
    } ?: true
}

private fun isUserExpertComparison(
    deal: Deal,
    userId: Long,
): Boolean =
    deal.admins.any {
        it.userId == userId
    }

fun getOppositeUserId(
    deal: Deal,
    userId: Long,
): Long {
    // Логика получения идентификатора противоположной стороны
    return if (deal.buyer.userId == userId) {
        deal.seller.userId
    } else {
        deal.buyer.userId
    }
}
