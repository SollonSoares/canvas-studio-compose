package com.canvasstudio.ui.block.utils

import android.graphics.Bitmap
import android.util.Log
import com.canvasstudio.domain.model.AnalyzedReceipt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptAnalyzer {

    private val KNOWN_INSTITUTIONS = listOf(
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

    suspend fun extractTextFromBitmap(bitmap: Bitmap): String {
        return try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            val text = result.text
            Log.d("ReceiptAnalyzer", "OCR extracted text (${text.length} chars): \n$text")
            text
        } catch (e: Exception) {
            Log.e("ReceiptAnalyzer", "OCR extraction failed: ${e.message}", e)
            ""
        }
    }

    fun analyze(
        rawText: String,
        fileName: String = ""
    ): AnalyzedReceipt {
        val cleanText = rawText.replace("\r", "\n")
        val lines = cleanText.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // 1. Identificar se é Pix
        val isPix = isPixPayment(cleanText, fileName)

        // 2. Extração do Valor Monetário
        val extractedValue = extractMonetaryValue(cleanText) ?: extractValueFromFileName(fileName)
        val formattedValue = extractedValue?.let { formatCurrency(it) }

        // 3. Extração da Data / "Realizado em"
        val extractedDate = extractRealizadoEm(cleanText)

        // 4. Extração Precisa de DE (Pagador) e PARA (Destinatário)
        val (pagador, destinatario) = extractParties(cleanText, lines)

        // 5. Extração de Instituição Bancária
        val instituicao = extractInstituicao(cleanText, lines)

        // 6. Determinação do Título / Descrição
        val extractedTitle = if (isPix) {
            when {
                destinatario != null -> "Pix - $destinatario"
                pagador != null -> "Pix - $pagador"
                instituicao != null -> "Pix - $instituicao"
                else -> "Comprovante Pix"
            }
        } else {
            extractTitle(cleanText, lines, fileName, destinatario ?: pagador)
        }

        Log.d("ReceiptAnalyzer", "Analysis: isPix=$isPix, title='$extractedTitle', valor=$extractedValue, date='$extractedDate', de='$pagador', para='$destinatario', inst='$instituicao'")

        return AnalyzedReceipt(
            title = extractedTitle,
            value = extractedValue,
            valueFormatted = formattedValue,
            realizadoEm = extractedDate,
            isPix = isPix,
            pagador = pagador,
            destinatario = destinatario,
            instituicao = instituicao,
            rawText = cleanText
        )
    }

    private fun isPixPayment(text: String, fileName: String): Boolean {
        val pixKeywords = listOf("PIX", "CHAVE PIX", "TRANSFERÊNCIA PIX", "ENVIO PIX", "PAGAMENTO PIX", "ID TRANSACAO", "END TO END")
        return pixKeywords.any { text.contains(it, ignoreCase = true) } || fileName.contains("PIX", ignoreCase = true)
    }

    private fun extractParties(text: String, lines: List<String>): Pair<String?, String?> {
        var pagador: String? = null
        var destinatario: String? = null

        // 1. Parsing Estruturado Linha a Linha por Seções de Contexto
        var currentSection = "" // "PAGADOR" or "RECEBEDOR"
        var linesSinceSection = 0

        for (i in lines.indices) {
            val line = lines[i]
            val lower = line.lowercase().trim()

            // Detecção estrita de cabeçalho de Pagador (DE / Origem)
            if (isPagadorHeader(lower)) {
                currentSection = "PAGADOR"
                linesSinceSection = 0
                // Verificar se o nome já está na mesma linha (Ex: "Pagador: JOAO SILVA")
                val inlineName = extractInlineName(line)
                if (inlineName != null && isValidPartyName(inlineName)) {
                    pagador = inlineName
                }
                continue
            }

            // Detecção estrita de cabeçalho de Recebedor (PARA / Destino)
            if (isRecebedorHeader(lower)) {
                currentSection = "RECEBEDOR"
                linesSinceSection = 0
                // Verificar se o nome já está na mesma linha (Ex: "Destinatário: MARIA OLIVEIRA")
                val inlineName = extractInlineName(line)
                if (inlineName != null && isValidPartyName(inlineName)) {
                    destinatario = inlineName
                }
                continue
            }

            // Se estiver dentro da seção do Pagador
            if (currentSection == "PAGADOR" && pagador == null && linesSinceSection < 4) {
                linesSinceSection++
                if (lower.startsWith("nome:") || lower.startsWith("nome ")) {
                    val candidate = cleanPartyName(line.substringAfter("nome").removePrefix(":"))
                    if (isValidPartyName(candidate)) pagador = candidate
                } else if (isValidPartyName(cleanPartyName(line)) && !isMetadataLine(lower)) {
                    pagador = cleanPartyName(line)
                }
            }

            // Se estiver dentro da seção do Recebedor
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

        // 2. Fallbacks de Regex Seguros e Específicos caso o loop não tenha capturado
        if (pagador == null) {
            val deRegex = "(?i)(?:DADOS\\s*DO\\s*PAGADOR|DADOS\\s*DE\\s*QUEM\\s*PAGOU|QUEM\\s*PAGOU|NOME\\s*DO\\s*PAGADOR)[^\\n]*\\n\\s*(?:NOME[:\\s]*)?([A-Za-zÀ-ÿ\\s\\.\\-&]{3,40})".toRegex()
            val match = deRegex.find(text)
            if (match != null) {
                val cand = cleanPartyName(match.groupValues[1])
                if (isValidPartyName(cand)) pagador = cand
            }
        }

        if (destinatario == null) {
            val paraRegex = "(?i)(?:DADOS\\s*DO\\s*RECEBEDOR|DADOS\\s*DE\\s*QUEM\\s*RECEBEU|QUEM\\s*RECEBEU|NOME\\s*DO\\s*DESTINAT[AÁ]RIO|NOME\\s*DO\\s*RECEBEDOR|NOME\\s*DO\\s*FAVORECIDO)[^\\n]*\\n\\s*(?:NOME[:\\s]*)?([A-Za-zÀ-ÿ\\s\\.\\-&]{3,40})".toRegex()
            val match = paraRegex.find(text)
            if (match != null) {
                val cand = cleanPartyName(match.groupValues[1])
                if (isValidPartyName(cand) && cand != pagador) destinatario = cand
            }
        }

        return Pair(pagador, destinatario)
    }

    private fun isPagadorHeader(lower: String): Boolean {
        // Nunca considerar preposições avulsas como "comprovante de..." ou "taxa de..."
        if (lower.contains("comprovante de") || lower.contains("documento de") || lower.contains("declaração de") || lower.contains("data de")) {
            return false
        }
        return lower.contains("dados do pagador") ||
               lower.contains("dados de quem pagou") ||
               lower.contains("quem pagou") ||
               lower.contains("conta de débito") ||
               lower.contains("conta de debito") ||
               lower.contains("debitado de") ||
               lower.contains("dados do remetente") ||
               lower.matches("^(?:pagador|origem|remetente)\\s*[:]?.*$".toRegex()) ||
               lower.matches("^de\\s*[:].*$".toRegex()) ||
               lower == "de"
    }

    private fun isRecebedorHeader(lower: String): Boolean {
        if (lower.contains("comprovante de") || lower.contains("documento de") || lower.contains("declaração de") || lower.contains("data de")) {
            return false
        }
        return lower.contains("dados do recebedor") ||
               lower.contains("dados de quem recebeu") ||
               lower.contains("quem recebeu") ||
               lower.contains("dados do favorecido") ||
               lower.contains("dados do destinatário") ||
               lower.contains("dados do destinatario") ||
               lower.contains("dados do beneficiário") ||
               lower.contains("dados do beneficiario") ||
               lower.contains("conta de crédito") ||
               lower.contains("conta de credito") ||
               lower.contains("creditado para") ||
               lower.matches("^(?:recebedor|destinatário|destinatario|favorecido|beneficiário|beneficiario|pago a)\\s*[:]?.*$".toRegex()) ||
               lower.matches("^para\\s*[:].*$".toRegex()) ||
               lower == "para"
    }

    private fun extractInlineName(line: String): String? {
        val colonIndex = line.indexOf(':')
        if (colonIndex != -1 && colonIndex < line.length - 1) {
            val afterColon = line.substring(colonIndex + 1).trim()
            if (afterColon.isNotBlank()) {
                return cleanPartyName(afterColon)
            }
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
        
        // Se for puramente números ou símbolos, não é um nome
        if (!clean.any { it.isLetter() }) return false

        val words = clean.lowercase().split("\\s+".toRegex())
        
        // Se todas as palavras forem termos genéricos bancários/fiscais, é inválido
        if (words.all { INVALID_NAME_WORDS.contains(it) }) return false

        // Se começa com palavras proibidas de documentos (Ex: "Comprovante", "Secretaria", "Transferência", "Pagamento")
        val forbiddenPrefixes = listOf("comprovante", "secretaria", "documento", "transferência", "transferencia", "pagamento", "extrato", "relatório", "recibo", "autenticação")
        if (forbiddenPrefixes.any { clean.startsWith(it, ignoreCase = true) }) return false

        return true
    }

    private fun extractInstituicao(text: String, lines: List<String>): String? {
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

    private fun extractRealizadoEm(text: String): String {
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

    private fun extractMonetaryValue(text: String): Float? {
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

    private fun extractValueFromFileName(fileName: String): Float? {
        val regex = "(?i)(?:R\\$|VALOR|_)([0-9]+(?:[_\\.,][0-9]{2})?)".toRegex()
        val match = regex.find(fileName)
        if (match != null) {
            val raw = match.groupValues[1].replace("_", ".").replace(",", ".")
            return raw.toFloatOrNull()
        }
        return null
    }

    private fun extractTitle(text: String, lines: List<String>, fileName: String, recipient: String?): String {
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

        if (recipient != null) {
            return recipient
        }

        if (text.contains("BOLETO", true) || fileName.contains("BOLETO", true)) {
            return "Pagamento de Boleto"
        }
        if (text.contains("TRANSFER[EÊ]NCIA", true)) {
            return "Transferência Bancária"
        }
        if (text.contains("COMPROVANTE", true)) {
            val firstLine = lines.firstOrNull { it.contains("COMPROVANTE", true) && it.length in 5..35 }
            if (firstLine != null) {
                return firstLine
            }
        }

        val cleanFileName = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ").trim()
        return if (cleanFileName.isNotBlank() && !cleanFileName.startsWith("temp", true) && !cleanFileName.equals("Comprovante", true)) {
            cleanFileName
        } else {
            "Comprovante"
        }
    }

    fun formatCurrency(value: Float): String {
        return String.format(Locale("pt", "BR"), "R$ %,.2f", value)
    }
}
