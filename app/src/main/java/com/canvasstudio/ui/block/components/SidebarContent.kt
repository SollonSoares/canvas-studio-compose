package com.canvasstudio.ui.block.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.data.local.entity.BlockEntity
import com.canvasstudio.ui.theme.CanvasColors
import kotlinx.serialization.json.*

@Composable
fun SidebarContent(
    q: String, 
    onQ: (String) -> Unit, 
    onSearch: () -> Unit, 
    onImp: () -> Unit, 
    onExp: () -> Unit,
    onClr: () -> Unit, 
    onOrg: () -> Unit, 
    modules: Map<String, Boolean>,
    onToggleModule: (String, Boolean) -> Unit, 
    isDarkMode: Boolean, 
    onToggleTheme: () -> Unit,
    isGridEnabled: Boolean, 
    onToggleGrid: () -> Unit,
    isLocked: Boolean, 
    onToggleLock: () -> Unit,
    onShowSettings: () -> Unit,
    onAddTextBlock: () -> Unit,
    onAddChartBlock: () -> Unit,
    onAddImageBlock: () -> Unit,
    onExportPdf: () -> Unit,
    onSharePdf: () -> Unit = {},
    onPickGalleryImage: () -> Unit = {},
    onExportToGallery: () -> Unit = {},
    selectedBlock: BlockEntity? = null,
    onDeselectBlock: () -> Unit = {},
    onUpdateTitle: (String) -> Unit = {},
    onUpdateTextFormatting: (fontSize: Int?, isBold: Boolean?, isItalic: Boolean?, align: String?, textColor: String?) -> Unit = { _, _, _, _, _ -> },
    onUpdateChartAttribute: (attribute: String, value: Float) -> Unit = { _, _ -> },
    onUpdateImageUrl: (url: String) -> Unit = {},
    onUpdateValue: (Float?) -> Unit = {},
    onUpdateRealizadoEm: (String) -> Unit = {},
    onUpdatePagador: (String) -> Unit = {},
    onUpdateDestinatario: (String) -> Unit = {},
    onUpdateInstituicao: (String) -> Unit = {},
    onDuplicateBlock: (BlockEntity) -> Unit = {},
    onDeleteBlock: (BlockEntity) -> Unit = {},
    colors: CanvasColors
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.bgMenu)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabeçalho do Menu
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "CANVAS STUDIO",
                color = colors.textMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                "v2.0",
                color = colors.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // ==========================================
        // 0. INSPETOR DE BLOCO SELECIONADO (Ou Dica)
        // ==========================================
        if (selectedBlock != null) {
            val metadata = remember(selectedBlock.contentJson) {
                try { Json.parseToJsonElement(selectedBlock.contentJson).jsonObject } catch (e: Exception) { null }
            }

            val currentFontSize = metadata?.get("fontSize")?.jsonPrimitive?.intOrNull 
                ?: metadata?.get("titleSize")?.jsonPrimitive?.intOrNull 
                ?: 13
            val isBold = metadata?.get("isBold")?.jsonPrimitive?.booleanOrNull ?: false
            val isItalic = metadata?.get("isItalic")?.jsonPrimitive?.booleanOrNull ?: false
            val currentAlign = metadata?.get("align")?.jsonPrimitive?.content ?: "left"
            val currentTextColor = metadata?.get("textColor")?.jsonPrimitive?.content
            val imageUrl = metadata?.get("url")?.jsonPrimitive?.content ?: ""

            val ninjutsu = metadata?.get("ninjutsu")?.jsonPrimitive?.floatOrNull ?: metadata?.get("nin")?.jsonPrimitive?.floatOrNull ?: 4f
            val inteligencia = metadata?.get("inteligencia")?.jsonPrimitive?.floatOrNull ?: metadata?.get("int")?.jsonPrimitive?.floatOrNull ?: 4f
            val chakra = metadata?.get("chakra")?.jsonPrimitive?.floatOrNull ?: metadata?.get("cha")?.jsonPrimitive?.floatOrNull ?: 4f
            val taijutsu = metadata?.get("taijutsu")?.jsonPrimitive?.floatOrNull ?: metadata?.get("tai")?.jsonPrimitive?.floatOrNull ?: 4f
            val vigor = metadata?.get("vigor")?.jsonPrimitive?.floatOrNull ?: metadata?.get("vig")?.jsonPrimitive?.floatOrNull ?: 4f
            val genjutsu = metadata?.get("genjutsu")?.jsonPrimitive?.floatOrNull ?: metadata?.get("gen")?.jsonPrimitive?.floatOrNull ?: 4f

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.bgCard)
                    .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header do Bloco Selecionado
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.accent.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                selectedBlock.type.uppercase(),
                                color = colors.accent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDeselectBlock,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            "Desmarcar Bloco",
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Edição de Título no Inspetor
                MenuSectionTitle("TÍTULO DO BLOCO", colors)
                var currentTitle by remember(selectedBlock.id, selectedBlock.title) { mutableStateOf(selectedBlock.title) }
                BasicTextField(
                    value = currentTitle,
                    onValueChange = {
                        currentTitle = it
                        onUpdateTitle(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgInput, RoundedCornerShape(6.dp))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    textStyle = TextStyle(
                        color = colors.textMain, 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (currentTitle.isEmpty()) Text("Título do bloco...", color = colors.textMuted, fontSize = 13.sp)
                        inner()
                    }
                )

                Divider(color = colors.borderSubtle)

                // Campo Valor do Comprovante (R$)
                val currentValor = remember(selectedBlock.contentJson) {
                    try {
                        val json = Json.parseToJsonElement(selectedBlock.contentJson).jsonObject
                        json["valor"]?.jsonPrimitive?.floatOrNull
                    } catch (e: Exception) { null }
                }
                
                MenuSectionTitle("VALOR (R$)", colors)
                var tempValorStr by remember(selectedBlock.id, currentValor) { 
                    mutableStateOf(currentValor?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "") 
                }
                BasicTextField(
                    value = tempValorStr,
                    onValueChange = {
                        tempValorStr = it
                        val num = it.replace(",", ".").toFloatOrNull()
                        onUpdateValue(num)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgInput, RoundedCornerShape(6.dp))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    textStyle = TextStyle(
                        color = Color(0xFF34C759), 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (tempValorStr.isEmpty()) Text("Ex: 150.00", color = colors.textMuted, fontSize = 13.sp)
                        inner()
                    }
                )

                // Campo Realizado Em (Data e Hora da Operação)
                val currentRealizadoEm = remember(selectedBlock.contentJson) {
                    try {
                        val json = Json.parseToJsonElement(selectedBlock.contentJson).jsonObject
                        json["realizadoEm"]?.jsonPrimitive?.contentOrNull
                    } catch (e: Exception) { null }
                }

                MenuSectionTitle("REALIZADO EM", colors)
                var tempRealizadoEmStr by remember(selectedBlock.id, currentRealizadoEm) { 
                    mutableStateOf(currentRealizadoEm ?: "") 
                }
                BasicTextField(
                    value = tempRealizadoEmStr,
                    onValueChange = {
                        tempRealizadoEmStr = it
                        onUpdateRealizadoEm(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgInput, RoundedCornerShape(6.dp))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    textStyle = TextStyle(
                        color = colors.textMain, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (tempRealizadoEmStr.isEmpty()) Text("Ex: 21/08/2026 14:30", color = colors.textMuted, fontSize = 12.sp)
                        inner()
                    }
                )

                // Campo Pagador (De / Origem)
                val currentPagador = remember(selectedBlock.contentJson) {
                    try {
                        val json = Json.parseToJsonElement(selectedBlock.contentJson).jsonObject
                        json["pagador"]?.jsonPrimitive?.contentOrNull
                    } catch (e: Exception) { null }
                }
                if (currentPagador != null || selectedBlock.title.contains("PIX", true)) {
                    MenuSectionTitle("DE (PAGADOR / ORIGEM)", colors)
                    var tempPagador by remember(selectedBlock.id, currentPagador) { 
                        mutableStateOf(currentPagador ?: "") 
                    }
                    BasicTextField(
                        value = tempPagador,
                        onValueChange = {
                            tempPagador = it
                            onUpdatePagador(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.bgInput, RoundedCornerShape(6.dp))
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        textStyle = TextStyle(color = colors.textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        cursorBrush = SolidColor(colors.accent),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (tempPagador.isEmpty()) Text("Nome de quem pagou...", color = colors.textMuted, fontSize = 12.sp)
                            inner()
                        }
                    )
                }

                // Campo Destinatário (Para / Destino)
                val currentDestinatario = remember(selectedBlock.contentJson) {
                    try {
                        val json = Json.parseToJsonElement(selectedBlock.contentJson).jsonObject
                        json["destinatario"]?.jsonPrimitive?.contentOrNull
                    } catch (e: Exception) { null }
                }
                if (currentDestinatario != null || selectedBlock.title.contains("PIX", true)) {
                    MenuSectionTitle("PARA (DESTINATÁRIO)", colors)
                    var tempDestinatario by remember(selectedBlock.id, currentDestinatario) { 
                        mutableStateOf(currentDestinatario ?: "") 
                    }
                    BasicTextField(
                        value = tempDestinatario,
                        onValueChange = {
                            tempDestinatario = it
                            onUpdateDestinatario(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.bgInput, RoundedCornerShape(6.dp))
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        textStyle = TextStyle(color = colors.textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        cursorBrush = SolidColor(colors.accent),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (tempDestinatario.isEmpty()) Text("Nome do recebedor...", color = colors.textMuted, fontSize = 12.sp)
                            inner()
                        }
                    )
                }

                // Campo Instituição Bancária
                val currentInstituicao = remember(selectedBlock.contentJson) {
                    try {
                        val json = Json.parseToJsonElement(selectedBlock.contentJson).jsonObject
                        json["instituicao"]?.jsonPrimitive?.contentOrNull
                    } catch (e: Exception) { null }
                }
                if (currentInstituicao != null || selectedBlock.title.contains("PIX", true)) {
                    MenuSectionTitle("INSTITUIÇÃO / BANCO", colors)
                    var tempInstituicao by remember(selectedBlock.id, currentInstituicao) { 
                        mutableStateOf(currentInstituicao ?: "") 
                    }
                    BasicTextField(
                        value = tempInstituicao,
                        onValueChange = {
                            tempInstituicao = it
                            onUpdateInstituicao(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.bgInput, RoundedCornerShape(6.dp))
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        textStyle = TextStyle(color = colors.textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        cursorBrush = SolidColor(colors.accent),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (tempInstituicao.isEmpty()) Text("Ex: Nu Pagamentos, Itaú...", color = colors.textMuted, fontSize = 12.sp)
                            inner()
                        }
                    )
                }

                Divider(color = colors.borderSubtle)

                when (selectedBlock.type.lowercase()) {
                    "chart" -> {
                        MenuSectionTitle("ATRIBUTOS DO RADAR", colors)
                        AttributeSlider("Ninjutsu", ninjutsu, { onUpdateChartAttribute("ninjutsu", it) }, colors)
                        AttributeSlider("Inteligência", inteligencia, { onUpdateChartAttribute("inteligencia", it) }, colors)
                        AttributeSlider("Chakra", chakra, { onUpdateChartAttribute("chakra", it) }, colors)
                        AttributeSlider("Taijutsu", taijutsu, { onUpdateChartAttribute("taijutsu", it) }, colors)
                        AttributeSlider("Vigor", vigor, { onUpdateChartAttribute("vigor", it) }, colors)
                        AttributeSlider("Genjutsu", genjutsu, { onUpdateChartAttribute("genjutsu", it) }, colors)
                    }
                    "image" -> {
                        MenuSectionTitle("URL DA IMAGEM", colors)
                        var tempUrl by remember(selectedBlock.id, imageUrl) { mutableStateOf(imageUrl) }
                        BasicTextField(
                            value = tempUrl,
                            onValueChange = {
                                tempUrl = it
                                onUpdateImageUrl(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.bgInput, RoundedCornerShape(6.dp))
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            textStyle = TextStyle(color = colors.textMain, fontSize = 12.sp),
                            cursorBrush = SolidColor(colors.accent),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (tempUrl.isEmpty()) Text("https://...", color = colors.textMuted, fontSize = 12.sp)
                                inner()
                            }
                        )
                    }
                    else -> {
                        // Formatação de Texto
                        MenuSectionTitle("TAMANHO DA FONTE", colors)
                        FontSizeSelector(
                            currentSize = currentFontSize,
                            onSizeSelected = { onUpdateTextFormatting(it, null, null, null, null) },
                            colors = colors
                        )

                        MenuSectionTitle("ESTILO & PESO", colors)
                        TextStyleToggle(
                            isBold = isBold,
                            onToggleBold = { onUpdateTextFormatting(null, !isBold, null, null, null) },
                            isItalic = isItalic,
                            onToggleItalic = { onUpdateTextFormatting(null, null, !isItalic, null, null) },
                            colors = colors
                        )

                        MenuSectionTitle("ALINHAMENTO", colors)
                        TextAlignSelector(
                            currentAlign = currentAlign,
                            onAlignSelected = { onUpdateTextFormatting(null, null, null, it, null) },
                            colors = colors
                        )

                        MenuSectionTitle("COR DO TEXTO", colors)
                        TextColorPalette(
                            currentColorHex = currentTextColor,
                            onColorSelected = { onUpdateTextFormatting(null, null, null, null, it) },
                            colors = colors
                        )
                    }
                }

                Divider(color = colors.borderSubtle)

                // Ações Rápidas do Bloco
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        SidebarButton(
                            "Duplicar",
                            Icons.Rounded.ContentCopy,
                            colors.textMain,
                            onClick = { onDuplicateBlock(selectedBlock) }
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        SidebarButton(
                            "Excluir",
                            Icons.Rounded.DeleteOutline,
                            colors.danger,
                            isDanger = true,
                            onClick = { onDeleteBlock(selectedBlock) }
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = colors.accent.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.2f))
            ) {
                Row(
                    Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.TouchApp,
                        null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Toque em um bloco no canvas para editar estilo e propriedades aqui.",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // 1. FILTRO
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("FILTRO", colors)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = q,
                    onValueChange = onQ,
                    modifier = Modifier
                        .weight(1f)
                        .background(colors.bgInput, RoundedCornerShape(6.dp))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    textStyle = TextStyle(color = colors.textMain, fontSize = 13.sp),
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (q.isEmpty()) {
                            Text("Buscar blocos...", color = colors.textMuted, fontSize = 13.sp)
                        }
                        innerTextField()
                    }
                )
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = onSearch,
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.accent, RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.Search, "Buscar", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // 2. CRIAR (Strict Web Alignment)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("CRIAR", colors)
            SidebarButton("Novo Bloco de Texto", Icons.Rounded.TextFields, colors.textMain, onClick = onAddTextBlock)
            SidebarButton("Novo Gráfico Radar", Icons.Rounded.BarChart, colors.textMain, onClick = onAddChartBlock)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.weight(1f)) {
                    SidebarButton("URL Imagem", Icons.Rounded.Image, colors.textMain, onClick = onAddImageBlock)
                }
                Box(Modifier.weight(1f)) {
                    SidebarButton("Foto Galeria", Icons.Rounded.AddPhotoAlternate, colors.textMain, onClick = onPickGalleryImage)
                }
            }
        }

        // 3. ORGANIZAÇÃO
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("ORGANIZAÇÃO", colors)
            SidebarButton("Auto Organizar", Icons.Rounded.AutoAwesome, colors.textMain, onClick = onOrg)
            SidebarButton("Limpar Tudo", Icons.Rounded.DeleteSweep, colors.danger, isDanger = true, onClick = onClr)
        }

        // 4. PORTABILIDADE
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("PORTABILIDADE", colors)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.weight(1f)) {
                    SidebarButton("JSON Export", Icons.Rounded.FileUpload, colors.textMain, onClick = onExp)
                }
                Box(Modifier.weight(1f)) {
                    SidebarButton("JSON Import", Icons.Rounded.FileDownload, colors.textMain, onClick = onImp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.weight(1f)) {
                    SidebarButton("Salvar PDF", Icons.Rounded.PictureAsPdf, colors.textMain, onClick = onExportPdf)
                }
                Box(Modifier.weight(1f)) {
                    SidebarButton("Compartilhar", Icons.Rounded.Share, colors.accent, onClick = onSharePdf)
                }
            }
            SidebarButton("Exportar para Galeria", Icons.Rounded.CloudUpload, colors.accent, onClick = onExportToGallery)
        }

        // Preferências & Footer
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("PREFERÊNCIAS", colors)
            SidebarButton("Configurações", Icons.Rounded.Settings, colors.textMain, onClick = onShowSettings)

            com.canvasstudio.designsystem.components.CanvasToggle(
                label = "Modo Escuro",
                checked = isDarkMode,
                onCheckedChange = { onToggleTheme() }
            )

            com.canvasstudio.designsystem.components.CanvasToggle(
                label = "Grade Visível",
                checked = isGridEnabled,
                onCheckedChange = { onToggleGrid() }
            )

            com.canvasstudio.designsystem.components.CanvasToggle(
                label = "Bloquear Edição",
                checked = isLocked,
                onCheckedChange = { onToggleLock() },
                activeColor = colors.danger
            )
        }

        Spacer(Modifier.weight(1f))
        Text(
            "v1.2.0 • Native Engine",
            color = colors.textMuted,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        )
    }
}

