package com.canvasstudio.ui.block.utils

object ReceiptInstitutionsCatalog {

    val KNOWN_INSTITUTIONS = listOf(
        "Nu Pagamentos S.A.", "Nubank", "Itaú Unibanco S.A.", "Itaú Unibanco", "Itaú",
        "Banco do Brasil S.A.", "Banco do Brasil", "Banco Bradesco S.A.", "Bradesco",
        "Banco Santander (Brasil) S.A.", "Santander", "Banco Inter S.A.", "Banco Inter", "Inter",
        "Mercado Pago ip Ltda", "Mercado Pago", "PicPay Servicos S.A.", "PicPay",
        "Banco C6 S.A.", "C6 Bank", "Caixa Econômica Federal", "Caixa Econômica", "Caixa",
        "Stone Pagamentos S.A.", "Stone", "PagSeguro Internet S.A.", "PagBank", "PagSeguro",
        "Cora SCD S.A.", "Cora", "Banco Neon", "Neon", "Banco BTG Pactual", "BTG Pactual",
        "Banco Safra S.A.", "Safra", "Sicredi", "Sicoob", "Banco Next", "Next",
        "Will Bank S.A.", "Will Bank", "Ame Digital"
    )

    fun extractInstituicao(text: String, lines: List<String>): String? {
        for (inst in KNOWN_INSTITUTIONS) {
            if (text.contains(inst, ignoreCase = true)) {
                return inst
            }
        }

        val instPatterns = listOf(
            "(?i)(?:INSTITUI[ÇC][ÃA]O\\s*DO\\s*RECEBEDOR|INSTITUI[ÇC][ÃA]O\\s*DESTINAT[AÁ]RIA|INSTITUI[ÇC][ÃA]O\\s*FINANCEIRA|INSTITUI[ÇC][ÃA]O|BANCO\\s*DO\\s*RECEBEDOR|BANCO\\s*DESTINO|BANCO)[:=]?\\s*(?:[0-9]{1,4}\\s*[-–]\\s*)?([A-Za-z0-9À-ÿ\\s\\.\\-&/]{3,40})".toRegex()
        )

        for (pattern in instPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val candidate = match.groupValues[1].split("\n").first().trim()
                if (candidate.length in 3..40 && !candidate.equals("Conta", true) && !candidate.equals("Agência", true)) {
                    return candidate
                }
            }
        }

        return null
    }
}
