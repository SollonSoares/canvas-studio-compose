package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
                letterSpacing = (-0.3).sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleLock,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        if (isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        "Lock",
                        tint = if (isLocked) colors.accent else colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onToggleGrid,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        if (isGridEnabled) Icons.Rounded.GridOn else Icons.Rounded.GridOff,
                        "Grid",
                        tint = if (isGridEnabled) colors.accent else colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 1. FILTRO (Search)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("FILTRO", colors)
            BasicTextField(
                value = q,
                onValueChange = onQ,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgInput, RoundedCornerShape(6.dp))
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                textStyle = TextStyle(color = colors.textMain, fontSize = 13.sp),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { innerTextField ->
                    if (q.isEmpty()) {
                        Text("Buscar blocos...", color = colors.textMuted, fontSize = 13.sp)
                    }
                    innerTextField()
                }
            )
        }

        // 2. CRIAR ELEMENTOS (Create)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("CRIAR ELEMENTOS", colors)
            // Aqui entram os botões de criar que serão adicionados em seguida
            SidebarButton("Novo Bloco de Texto", Icons.Rounded.TextFields, colors.textMain, onClick = { /* TODO */ })
            SidebarButton("Novo Gráfico", Icons.Rounded.BarChart, colors.textMain, onClick = { /* TODO */ })
        }

        // 3. ORGANIZAÇÃO & PALCO
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("ORGANIZAÇÃO & PALCO", colors)
            SidebarButton("Auto Organizar", Icons.Rounded.AutoAwesome, colors.textMain, onClick = onOrg)
            SidebarButton("Limpar Tudo", Icons.Rounded.DeleteSweep, colors.danger, isDanger = true, onClick = onClr)
        }

        // 4. PORTABILIDADE
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuSectionTitle("PORTABILIDADE", colors)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.weight(1f)) {
                    SidebarButton("Exportar", Icons.Rounded.FileUpload, colors.textMain, onClick = onExp)
                }
                Box(Modifier.weight(1f)) {
                    SidebarButton("Importar", Icons.Rounded.FileDownload, colors.textMain, onClick = onImp)
                }
            }
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
