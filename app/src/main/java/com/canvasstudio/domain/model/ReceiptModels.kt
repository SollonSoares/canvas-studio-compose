package com.canvasstudio.domain.model

data class AnalyzedReceipt(
    val title: String,
    val value: Float?,
    val valueFormatted: String?,
    val realizadoEm: String,
    val isPix: Boolean = false,
    val pagador: String? = null,
    val destinatario: String? = null,
    val instituicao: String? = null,
    val rawText: String = ""
)

sealed class ReceiptType {
    object Pix : ReceiptType()
    object Tax : ReceiptType()
    object Boleto : ReceiptType()
    object Transfer : ReceiptType()
    object Generic : ReceiptType()
}
