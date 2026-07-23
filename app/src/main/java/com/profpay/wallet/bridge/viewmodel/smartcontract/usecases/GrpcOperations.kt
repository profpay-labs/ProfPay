package com.profpay.wallet.bridge.viewmodel.smartcontract.usecases

import com.profpay.domain.contract.model.Deal
import com.profpay.domain.contract.model.DealChangeStatus
import com.profpay.domain.contract.model.params.CallContractParams
import com.profpay.domain.contract.model.params.DealStatusChangeParams
import com.profpay.domain.contract.model.params.DisputeActionParams
import com.profpay.domain.contract.repository.ContractRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import javax.inject.Inject

class GrpcOperations
@Inject
constructor(
    private val contractRepository: ContractRepository,
    private val profileLocalRepository: ProfileLocalRepository,
) {
    suspend fun contractDealStatusChanged(
        deal: Deal,
        contractAddress: String,
        status: DealChangeStatus,
    ) {
        contractRepository.processDealStatusChange(
            DealStatusChangeParams(
                dealId = deal.dealId,
                blockchainDealId = deal.blockchainDealId,
                userId = profileLocalRepository.getUserId(),
                contractAddress = contractAddress,
                changeStatus = status,
            )
        )
    }

    suspend fun contractDealStatusExpertChanged(
        deal: Deal,
        contractAddress: String,
        status: DealChangeStatus,
    ) {
        contractRepository.processDisputeAction(
            DisputeActionParams(
                dealId = deal.dealId,
                initiatorUserId = profileLocalRepository.getUserId(),
                contractAddress = contractAddress,
                action = status,
            )
        )
    }

    suspend fun callContract(params: CallContractParams) {
        contractRepository.callContract(params)
    }
}
