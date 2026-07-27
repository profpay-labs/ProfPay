package com.profpay.wallet.presentation.viewmodel.smartcontract.usecases

import com.google.protobuf.ByteString
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.core.tron.model.CreateDealParams
import com.profpay.domain.contract.model.Deal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

class BlockchainOperations
    @Inject
    constructor(
        private val addressLocalRepository: AddressLocalRepository,
        private val tron: Tron,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) {
        suspend fun createDeal(deal: Deal): DealActionResult {
            val addressData =
                withContext(ioDispatcher) {
                    addressLocalRepository.getByAddress(deal.buyer.walletAddress)
                }

            if (addressData == null) throw Error("Not address")

            val params = CreateDealParams(
                sellerAddress = deal.seller.walletAddress,
                buyerAddress = deal.buyer.walletAddress,
                amount = deal.amount,
                admins = deal.admins.map { admin ->
                    CreateDealParams.AdminInfo(
                        walletAddress = admin.walletAddress,
                        tierName = admin.tier.name,
                    )
                },
            )

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.createDeal(
                        ownerAddress = addressData.address,
                        contractAddress = deal.contractAddress,
                        privateKey = "addressData.privateKey",
                        params = params,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun approveAndDepositDeal(deal: Deal): DealActionResult {
            val addressData =
                withContext(ioDispatcher) {
                    addressLocalRepository.getByAddress(deal.buyer.walletAddress)
                } ?: return DealActionResult.Error("Address data is null")

            val isAllowanceUnlimited =
                tron.accounts.isAllowanceUnlimited(
                    spender = deal.contractAddress,
                    ownerAddress = addressData.address,
                    privateKey = "addressData.privateKey",
                )

            if (!isAllowanceUnlimited) {
                val signedTransaction: ByteString =
                    withContext(ioDispatcher) {
                        tron.smartContracts.multiSigWrite.approve(
                            ownerAddress = deal.buyer.walletAddress,
                            privateKey = "addressData.privateKey",
                            contractAddress = deal.contractAddress,
                        )
                    }
                return DealActionResult.Success(signedTransaction)
            }

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.depositDeal(
                        id = deal.blockchainDealId,
                        ownerAddress = deal.buyer.walletAddress,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.contractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun approveAndPaySellerExpertFee(
            deal: Deal,
            userId: Long,
        ): DealActionResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.walletAddress
                } else {
                    deal.seller.walletAddress
                }

            val addressData =
                withContext(ioDispatcher) {
                    addressLocalRepository.getByAddress(address)
                } ?: return DealActionResult.Error("Address data is null")

            val allowanceValue =
                tron.accounts.allowance(
                    spender = deal.contractAddress,
                    ownerAddress = addressData.address,
                    privateKey = "addressData.privateKey",
                )

            val approveAmount = deal.blockchainData!!.totalExpertCommissions / 2
            val approveCompare = allowanceValue!!.compareTo(approveAmount.toBigInteger()) == -1

            if (approveCompare) {
                val signedTransaction: ByteString =
                    withContext(ioDispatcher) {
                        tron.smartContracts.multiSigWrite.approve(
                            ownerAddress = address,
                            privateKey = "addressData.privateKey",
                            contractAddress = deal.contractAddress,
                        )
                    }
                return DealActionResult.Success(signedTransaction)
            }

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.paySellerExpertFee(
                        id = deal.blockchainDealId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.contractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun confirmDeal(
            deal: Deal,
            userId: Long,
        ): DealActionResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.walletAddress
                } else {
                    deal.seller.walletAddress
                }

            val addressData =
                withContext(ioDispatcher) {
                    addressLocalRepository.getByAddress(address)
                }

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.voteDeal(
                        id = deal.blockchainDealId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.contractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun rejectCancelDeal(
            deal: Deal,
            userId: Long,
        ): DealActionResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.walletAddress
                } else {
                    deal.seller.walletAddress
                }

            val addressData =
                withContext(ioDispatcher) {
                    addressLocalRepository.getByAddress(address)
                }

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.cancelDeal(
                        id = deal.blockchainDealId,
                        ownerAddress = addressData.address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.contractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun executeDisputed(
            deal: Deal,
            userId: Long,
        ): DealActionResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.walletAddress
                } else {
                    deal.seller.walletAddress
                }

            val addressData =
                withContext(ioDispatcher) {
                    addressLocalRepository.getByAddress(address)
                }

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.executeDisputed(
                        id = deal.blockchainDealId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.contractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun assignDecisionAdminAndSetAmounts(
            deal: Deal,
            userId: Long,
            sellerValue: BigInteger,
            buyerValue: BigInteger,
        ): DealActionResult {
            val admin = deal.admins.find { it.userId == userId } ?: return DealActionResult.Error("None admin")

            val addressData =
                withContext(ioDispatcher) {
                    addressLocalRepository.getByAddress(admin.walletAddress)
                }

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.assignDecisionAdminAndSetAmounts(
                        id = deal.blockchainDealId,
                        ownerAddress = admin.walletAddress,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.contractAddress,
                        sellerValue = sellerValue,
                        buyerValue = buyerValue,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun voteOnDisputeResolution(
            deal: Deal,
            userId: Long,
        ): DealActionResult {
            val address =
                deal.admins.firstOrNull { it.userId == userId }?.walletAddress
                    ?: when (userId) {
                        deal.buyer.userId -> {
                            deal.buyer.walletAddress
                        }
                        deal.seller.userId -> {
                            deal.seller.walletAddress
                        }
                        else -> {
                            return DealActionResult.Error("None address")
                        }
                    }

            val addressData =
                withContext(ioDispatcher) {
                    addressLocalRepository.getByAddress(address)
                }

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.voteOnDisputeResolution(
                        id = deal.blockchainDealId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.contractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun declineDisputeResolution(
            deal: Deal,
            userId: Long,
        ): DealActionResult {
            val address =
                deal.admins.firstOrNull { it.userId == userId }?.walletAddress
                    ?: when (userId) {
                        deal.buyer.userId -> {
                            deal.buyer.walletAddress
                        }
                        deal.seller.userId -> {
                            deal.seller.walletAddress
                        }
                        else -> {
                            return DealActionResult.Error("None address")
                        }
                    }

            val addressData =
                withContext(ioDispatcher) {
                    addressLocalRepository.getByAddress(address)
                }

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.declineDisputeResolution(
                        id = deal.blockchainDealId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.contractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

//        private fun estimateTransactionCost(
//            function: Function,
//            contractAddress: String,
//            addressData: AddressEntity,
//        ): BigInteger {
//            val estimate =
//                tron.transactions.estimateEnergy(
//                    function = function,
//                    contractAddress = contractAddress,
//                    address = addressData.address,
//                    privateKey = "addressData.privateKey".toByteArray(),
//                )
//            return estimate.energyInTrx
//        }

        private fun getBalance(address: String): BigInteger = tron.addressUtilities.getTrxBalance(address)
    }

sealed class DealActionResult(
    open val transaction: ByteString? = null,
    open val amountRequired: BigInteger? = null,
    open val reason: String? = null,
) {
    data class Success(
        override val transaction: ByteString,
    ) : DealActionResult(transaction = transaction)

    data class InsufficientFunds(
        val type: Type,
        override val amountRequired: BigInteger,
    ) : DealActionResult(amountRequired = amountRequired) {
        enum class Type { BALANCE, APPROVAL }
    }

    data class Error(
        override val reason: String,
    ) : DealActionResult(reason = reason)
}
