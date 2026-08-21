package com.canvasstudio.ui.block.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptFinancialExtractors {

    fun isPixPayment(text: String, fileName: String): Boolean {
        val pixKeywords = listOf("PIX", "CHAVE PIX", "TRANSFERÊNCIA PIX", "ENVIO PIX", "PAGAMENTO PIX", "ID TRANSACAO", "END TO END")
        return pixKeywords.any { text.contains(it, ignoreCase = true) } || fileName.contains("PIX", ignoreCase = true)
    }

    fun extractMonetaryValue(text: String): Float? {
        val patterns = listOf(
            "(?i)(?:VALOR\\s*TOTAL|VALOR\\s*PAGO|VALOR\\s*PRINCIPAL|VALOR\\s*L[ÍI]QUIDO|VALOR\\s*DO\\s*DOCUMENTO|VALOR\\s*COBRADO|VALOR\\s*DA\\s*OPERA[ÇC][ÃA]O|VALOR)\\s*[:=]?\\s*R?\\$\\s*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})".toRegex(),
            "(?i)TOTAL\\s*[:=]?\\s*R?\\$\\s*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})".toRegex(),
            "(?i)R\\$\\s*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})".toRegex(),
            "(?i)([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})\\s*(?:BRL|REAIS)".toRegex(),
            "(?i)R\\$\\s*([0-9]+(?:\\.[0-9]{2}))\\b".toRegex(),
            "\\b([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})\\b".toRegex()
        )

        for (pattern in patterns) {
            val matches = pattern.findAll(text).toList()
            for (match in matches) {
                val rawVal = match.groupValues[1]
                val normalized = rawVal.replace(".", "").replace(",", ".")
                val num = normalized.toFloatOrNull()
                if (num != null && num > 0f) {
                    return num
                }
            }
        }
        return null
    }

    fun extractValueFromFileName(fileName: String): Float? {
        val regex = "(?i)(?:R\\$|VALOR|_)([0-9]+(?:[_\\.,][0-9]{2})?)".toRegex()
        val match = regex.find(fileName)
        if (match != null) {
            val raw = match.groupValues[1].replace("_", ".").replace(",", ".")
            return raw.toFloatOrNull()
        }
        return null
    }

    fun extractRealizadoEm(text: String): String {
        val datePatterns = listOf(
            "(?i)(?:REALIZADO\\s*EM|EFETUADO\\s*EM|PAGO\\s*EM|DATA\\s*DO\\s*PAGAMENTO|DATA\\s*DA\\s*OPERA[ÇC][ÃA]O|DATA\\s*DE\\s*PAGAMENTO|DATA\\s*DA\\s*TRANSFER[EÊ]NCIA|DATA\\s*E\\s*HORA)\\s*[:=]?\\s*([0-9]{2}/[0-9]{2}/[0-9]{4}(?:\\s*(?:[A-Za-zà-ÿ\\-]+)?\\s*[0-9]{2}:[0-9]{2}(?::[0-9]{2})?)?)".toRegex(),
            "\\b([0-9]{2}/[0-9]{2}/[0-9]{4}(?:\\s*(?:[àa]s|-)?\\s*[0-9]{2}:[0-9]{2}(?::[0-9]{2})?)?)\\b".toRegex(),
            "\\b([0-9]{2}/[0-9]{2}/[0-9]{4})\\b".toRegex()
        )

        for (pattern in datePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val found = match.groupValues[1].trim()
                if (found.length >= 8) {
                    return found
                }
            }
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        return sdf.format(Date())
    }

    fun formatCurrency(value: Float): String {
        return String.format(Locale("pt", "BR"), "R$ %,.2f", value)
    }
}
