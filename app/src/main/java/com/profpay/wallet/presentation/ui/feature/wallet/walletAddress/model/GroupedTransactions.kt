package com.profpay.wallet.presentation.ui.feature.wallet.walletAddress.model

import com.profpay.domain.wallet.model.TransactionSummary


data class GroupedTransactions(
    var all: List<List<TransactionSummary?>>,
    var sender: List<List<TransactionSummary?>>,
    var receiver: List<List<TransactionSummary?>>,
)
