package com.canvasstudio.ui.block.utils

object ReceiptPartiesExtractor {

    private val INVALID_NAME_WORDS = setOf(
        "comprovante", "pagamento", "transferencia", "transferência", "dados", "conta",
        "agência", "agencia", "extrato", "banco", "autenticação", "autenticacao", "pix",
        "valor", "data", "horário", "horario", "documento", "arrecadação", "arrecadacao",
        "segunda", "via", "canal", "chave", "instituição", "instituicao", "código",
        "codigo", "itau", "itaú", "bradesco", "santander", "nubank", "caixa", "recibo",
        "operação", "operacao", "transação", "transacao", "identificação", "identificacao",
        "protocolo", "liquidado", "efetuado", "realizado", "sucesso", "débito", "debito",
        "crédito", "credito", "total", "principal", "juros", "multa", "desconto",
        "favorecido", "recebedor", "pagador", "destinatario", "destinatário", "origem",
        "destino", "beneficiario", "beneficiário", "remetente", "nome", "tipo"
    )

    fun extractParties(text: String, lines: List<String>): Pair<String?, String?> {
        var pagador: String? = null
        var destinatario: String? = null
        var currentSection = ""
        var linesSinceSection = 0

        for (i in lines.indices) {
            val line = lines[i]
            val lower = line.lowercase().trim()

            if (isPagadorHeader(lower)) {
                currentSection = "PAGADOR"
                linesSinceSection = 0
                val inlineName = extractInlineName(line)
                if (inlineName != null && isValidPartyName(inlineName)) pagador = inlineName
                continue
            }

            if (isRecebedorHeader(lower)) {
                currentSection = "RECEBEDOR"
                linesSinceSection = 0
                val inlineName = extractInlineName(line)
                if (inlineName != null && isValidPartyName(inlineName)) destinatario = inlineName
                continue
            }

            if (currentSection == "PAGADOR" && pagador == null && linesSinceSection < 4) {
                linesSinceSection++
                if (lower.startsWith("nome:") || lower.startsWith("nome ")) {
                    val candidate = cleanPartyName(line.substringAfter("nome").removePrefix(":"))
                    if (isValidPartyName(candidate)) pagador = candidate
                } else if (isValidPartyName(cleanPartyName(line)) && !isMetadataLine(lower)) {
                    pagador = cleanPartyName(line)
                }
            }

            if (currentSection == "RECEBEDOR" && destinatario == null && linesSinceSection < 4) {
                linesSinceSection++
                if (lower.startsWith("nome:") || lower.startsWith("nome ")) {
                    val candidate = cleanPartyName(line.substringAfter("nome").removePrefix(":"))
                    if (isValidPartyName(candidate) && candidate != pagador) destinatario = candidate
                } else if (isValidPartyName(cleanPartyName(line)) && !isMetadataLine(lower) && cleanPartyName(line) != pagador) {
                    destinatario = cleanPartyName(line)
                }
            }
        }

        if (pagador == null) {
            val deRegex = "(?i)(?:DADOS\\s*DO\\s*PAGADOR|DADOS\\s*DE\\s*QUEM\\s*PAGOU|QUEM\\s*PAGOU|NOME\\s*DO\\s*PAGADOR)[^\\n]*\\n\\s*(?:NOME[:\\s]*)?([A-Za-zÀ-ÿ\\s\\.\\-&]{3,40})".toRegex()
            deRegex.find(text)?.let {
                val cand = cleanPartyName(it.groupValues[1])
                if (isValidPartyName(cand)) pagador = cand
            }
        }

        if (destinatario == null) {
            val paraRegex = "(?i)(?:DADOS\\s*DO\\s*RECEBEDOR|DADOS\\s*DE\\s*QUEM\\s*RECEBEU|QUEM\\s*RECEBEU|NOME\\s*DO\\s*DESTINAT[AÁ]RIO|NOME\\s*DO\\s*RECEBEDOR|NOME\\s*DO\\s*FAVORECIDO)[^\\n]*\\n\\s*(?:NOME[:\\s]*)?([A-Za-zÀ-ÿ\\s\\.\\-&]{3,40})".toRegex()
            paraRegex.find(text)?.let {
                val cand = cleanPartyName(it.groupValues[1])
                if (isValidPartyName(cand) && cand != pagador) destinatario = cand
            }
        }

        return Pair(pagador, destinatario)
    }

    fun extractTitle(text: String, lines: List<String>, fileName: String, recipient: String?): String {
        return ReceiptTitleDetector.extractTitle(text, lines, fileName, recipient)
    }

    private fun isPagadorHeader(lower: String): Boolean {
        if (lower.contains("comprovante de") || lower.contains("documento de") || lower.contains("declaração de") || lower.contains("data de")) return false
        return lower.contains("dados do pagador") || lower.contains("dados de quem pagou") || lower.contains("quem pagou") ||
               lower.contains("conta de débito") || lower.contains("conta de debito") || lower.contains("debitado de") ||
               lower.contains("dados do remetente") || lower.matches("^(?:pagador|origem|remetente)\\s*[:]?.*$".toRegex()) ||
               lower.matches("^de\\s*[:].*$".toRegex()) || lower == "de"
    }

    private fun isRecebedorHeader(lower: String): Boolean {
        if (lower.contains("comprovante de") || lower.contains("documento de") || lower.contains("declaração de") || lower.contains("data de")) return false
        return lower.contains("dados do recebedor") || lower.contains("dados de quem recebeu") || lower.contains("quem recebeu") ||
               lower.contains("dados do favorecido") || lower.contains("dados do destinatário") || lower.contains("dados do destinatario") ||
               lower.contains("dados do beneficiário") || lower.contains("dados do beneficiario") || lower.contains("conta de crédito") ||
               lower.contains("conta de credito") || lower.contains("creditado para") ||
               lower.matches("^(?:recebedor|destinatário|destinatario|favorecido|beneficiário|beneficiario|pago a)\\s*[:]?.*$".toRegex()) ||
               lower.matches("^para\\s*[:].*$".toRegex()) || lower == "para"
    }

    private fun extractInlineName(line: String): String? {
        val colonIndex = line.indexOf(':')
        if (colonIndex != -1 && colonIndex < line.length - 1) {
            val afterColon = line.substring(colonIndex + 1).trim()
            if (afterColon.isNotBlank()) return cleanPartyName(afterColon)
        }
        return null
    }

    private fun isMetadataLine(lower: String): Boolean {
        val metaTokens = listOf("cpf", "cnpj", "chave", "instituição", "instituicao", "banco", "agência", "agencia", "conta", "tipo de chave", "valor", "data", "id da transação", "autenticação")
        return metaTokens.any { lower.contains(it) }
    }

    private fun cleanPartyName(raw: String): String {
        return raw.split("\n").first()
            .replace("(?i)^(Nome[:\\s]*|Para[:\\s]*|De[:\\s]*|Pagador[:\\s]*|Destinatário[:\\s]*|Favorecido[:\\s]*)".toRegex(), "")
            .replace("(?i)(CPF|CNPJ|Chave|Instituição|Instituicao|Banco|Agência|Agencia|Conta|Valor|Data).*$".toRegex(), "")
            .trim()
    }

    private fun isValidPartyName(name: String): Boolean {
        val clean = name.trim()
        if (clean.length < 3 || clean.length > 45) return false
        if (!clean.any { it.isLetter() }) return false
        val words = clean.lowercase().split("\\s+".toRegex())
        if (words.all { INVALID_NAME_WORDS.contains(it) }) return false
        val forbiddenPrefixes = listOf("comprovante", "secretaria", "documento", "transferência", "transferencia", "pagamento", "extrato", "relatório", "recibo", "autenticação")
        if (forbiddenPrefixes.any { clean.startsWith(it, ignoreCase = true) }) return false
        return true
    }
}
