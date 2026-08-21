package com.canvasstudio.ui.block.utils

object ReceiptTitleDetector {

    fun extractTitle(text: String, lines: List<String>, fileName: String, recipient: String?): String {
        val taxKeywords = listOf(
            "(?i)\\b(SEFAZ[-_\\s]?[A-Z0-9]+[-_\\s]?[A-Z0-9]+)\\b".toRegex(),
            "(?i)\\b(SEFAZ[-_\\s]?[A-Z]{2})\\b".toRegex(),
            "(?i)\\b(DARF(?:\\s*NUMERADO|\\s*WEB|\\s*SIMPLES)?)\\b".toRegex(),
            "(?i)\\b(DAS[-_\\s]?(?:MEI|SIMPLES)?)\\b".toRegex(),
            "(?i)\\b(GPS[-_\\s]?(?:PREVID[EÊ]NCIA)?)\\b".toRegex(),
            "(?i)\\b(IPVA\\s*[0-9]{4}|IPTU\\s*[0-9]{4}|ITBI)\\b".toRegex(),
            "(?i)\\b(DOCUMENTO\\s*DE\\s*ARRECADA[ÇC][ÃA]O[-_\\s]?[A-Z0-9]*)\\b".toRegex()
        )

        for (pattern in taxKeywords) {
            val match = pattern.find(text) ?: pattern.find(fileName)
            if (match != null) {
                return match.groupValues[1].trim().uppercase().replace(" ", "-").replace("_", "-")
            }
        }

        if (recipient != null) return recipient

        if (text.contains("BOLETO", true) || fileName.contains("BOLETO", true)) return "Pagamento de Boleto"
        if (text.contains("TRANSFER[EÊ]NCIA", true)) return "Transferência Bancária"

        if (text.contains("COMPROVANTE", true)) {
            val firstLine = lines.firstOrNull { it.contains("COMPROVANTE", true) && it.length in 5..35 }
            if (firstLine != null) return firstLine
        }

        val cleanFileName = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ").trim()
        return if (cleanFileName.isNotBlank() && !cleanFileName.startsWith("temp", true) && !cleanFileName.equals("Comprovante", true)) {
            cleanFileName
        } else {
            "Comprovante"
        }
    }
}
