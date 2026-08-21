package com.canvasstudio.ui.block.components

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SidebarContent(
    q: String, 
    onQ: (String) -> Unit, 
    onSearch: () -> Unit, 
    onImp: () -> Unit, 
    onExp: () -> Unit,
    onClr: () -> Unit, 
    onGen: () -> Unit, 
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
        // Cabeçalho do Menu (ORGANIZAÇÃO & PALCO - Adaptado para o topo)
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
            SidebarButton("Inserir Imagem", Icons.Rounded.Image, colors.textMain, onClick = onAddImageBlock)
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
            SidebarButton("Exportar PDF (Native)", Icons.Rounded.PictureAsPdf, colors.textMain, onClick = onExportPdf)
        }

        // Preferências & Footer
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("PREFERÊNCIAS", colors)
            SidebarButton("Configurações", Icons.Rounded.Settings, colors.textMain, onClick = onShowSettings)

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MODO ESCURO", color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF30D158),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(0.35f)
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }
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
