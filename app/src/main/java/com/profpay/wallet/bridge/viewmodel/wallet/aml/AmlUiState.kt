package com.profpay.wallet.bridge.viewmodel.wallet.aml

import com.profpay.domain.aml.model.AmlReport

sealed interface AmlUiState {
    data object Idle : AmlUiState
    data object Loading : AmlUiState
    data class Success(val report: AmlReport) : AmlUiState
    data class Error(val message: String) : AmlUiState
}

sealed interface AmlPaymentUiEvent {
    data object Idle : AmlPaymentUiEvent
    data object Loading : AmlPaymentUiEvent
    data class Success(val message: String) : AmlPaymentUiEvent
    data class Error(val title: String, val message: String) : AmlPaymentUiEvent
}

sealed interface PdfDownloadUiEvent {
    data object Idle : PdfDownloadUiEvent
    data object Loading : PdfDownloadUiEvent
    data class Success(val pdfBytes: ByteArray) : PdfDownloadUiEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Success
            return pdfBytes.contentEquals(other.pdfBytes)
        }
        override fun hashCode(): Int = pdfBytes.contentHashCode()
    }
    data class Error(val message: String) : PdfDownloadUiEvent
}
