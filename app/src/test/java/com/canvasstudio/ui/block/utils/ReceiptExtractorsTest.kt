package com.canvasstudio.ui.block.utils

import org.junit.Assert.*
import org.junit.Test

class ReceiptExtractorsTest {

    @Test
    fun `extractMonetaryValue extrai formatos com R$ e decimais`() {
        assertEquals(1500.50f, ReceiptFinancialExtractors.extractMonetaryValue("Valor pago: R$ 1.500,50")!!, 0.001f)
        assertEquals(120.00f, ReceiptFinancialExtractors.extractMonetaryValue("Total: R$ 120,00")!!, 0.001f)
        assertEquals(49.90f, ReceiptFinancialExtractors.extractMonetaryValue("VALOR LIQUIDADO: 49,90")!!, 0.001f)
    }

    @Test
    fun `extractValueFromFileName extrai valores do nome do arquivo`() {
        assertEquals(150.00f, ReceiptFinancialExtractors.extractValueFromFileName("comprovante_pix_150_00.jpg")!!, 0.001f)
        assertEquals(2350.50f, ReceiptFinancialExtractors.extractValueFromFileName("recibo_2350,50_itau.png")!!, 0.001f)
    }

    @Test
    fun `isPixPayment detecta variantes de PIX em texto e arquivo`() {
        assertTrue(ReceiptFinancialExtractors.isPixPayment("Transferência Pix realizada", ""))
        assertTrue(ReceiptFinancialExtractors.isPixPayment("Autenticação bancária", "pix_nubank.pdf"))
        assertTrue(ReceiptFinancialExtractors.isPixPayment("Chave Pix CPF: 123.456.789-00", ""))
        assertFalse(ReceiptFinancialExtractors.isPixPayment("TED para outra conta", "ted.pdf"))
    }

    @Test
    fun `extractRealizadoEm extrai formatos de data com barra e hora`() {
        val text = "Comprovante\nData da operação: 21/08/2026 às 14:35:10\nValor: R$ 100,00"
        val date = ReceiptFinancialExtractors.extractRealizadoEm(text)
        assertTrue(date.contains("21/08/2026"))
    }

    @Test
    fun `extractInstituicao identifica bancos principais`() {
        val linesNubank = listOf("Nubank", "Comprovante de Transferência")
        assertEquals("Nubank", ReceiptInstitutionsCatalog.extractInstituicao("Transferência via Nubank", linesNubank))

        val linesItau = listOf("Itaú", "Comprovante")
        assertEquals("Itaú", ReceiptInstitutionsCatalog.extractInstituicao("Conta Itaú", linesItau))

        val linesInter = listOf("Banco Inter", "Pix Realizado")
        assertEquals("Banco Inter", ReceiptInstitutionsCatalog.extractInstituicao("Banco Inter", linesInter))
    }

    @Test
    fun `extractParties identifica pagador e destinatario em comprovante estruturado`() {
        val rawText = """
            COMPROVANTE DE TRANSFERÊNCIA PIX
            DADOS DO PAGADOR
            Nome: CARLOS ALBERTO SILVA
            CPF: ***.123.456-**
            Instituição: Nubank
            
            DADOS DO RECEBEDOR
            Nome: MARIA DE SOUZA OLIVEIRA
            CNPJ: **.***.***/0001-**
            Instituição: Itaú
            
            Valor: R$ 350,00
            Data: 21/08/2026
        """.trimIndent()
        val lines = rawText.split("\n").map { it.trim() }

        val (pagador, destinatario) = ReceiptPartiesExtractor.extractParties(rawText, lines)

        assertEquals("CARLOS ALBERTO SILVA", pagador)
        assertEquals("MARIA DE SOUZA OLIVEIRA", destinatario)
    }

    @Test
    fun `extractTitle identifica tributos oficiais`() {
        assertEquals("DARF-WEB", ReceiptTitleDetector.extractTitle("COMPROVANTE DARF WEB", emptyList(), "", null))
        assertEquals("SEFAZ-SP", ReceiptTitleDetector.extractTitle("ARRECADAÇÃO SEFAZ SP", emptyList(), "", null))
        assertEquals("GPS-PREVIDENCIA", ReceiptTitleDetector.extractTitle("GUIA GPS PREVIDENCIA", emptyList(), "", null))
    }

    @Test
    fun `ReceiptAnalyzer analyze gera AnalyzedReceipt completo`() {
        val rawText = """
            COMPROVANTE PIX
            De: ALICE PEREIRA
            Para: BRUNO LIMA
            Valor: R$ 89,90
            Realizado em: 21/08/2026
            Banco: Nubank
        """.trimIndent()

        val receipt = ReceiptAnalyzer.analyze(rawText, "comprovante.jpg")

        assertTrue(receipt.isPix)
        assertEquals(89.90f, receipt.value!!, 0.001f)
        assertEquals("ALICE PEREIRA", receipt.pagador)
        assertEquals("BRUNO LIMA", receipt.destinatario)
        assertEquals("Nubank", receipt.instituicao)
        assertTrue(receipt.title.contains("BRUNO LIMA"))
    }
}
