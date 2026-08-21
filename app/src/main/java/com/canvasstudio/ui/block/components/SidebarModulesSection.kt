package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.components.CanvasButton
import com.canvasstudio.designsystem.components.CanvasButtonVariant
import com.canvasstudio.designsystem.components.CanvasToggle
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SidebarModulesSection(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    isGridEnabled: Boolean,
    onToggleGrid: () -> Unit,
    isLocked: Boolean,
    onToggleLock: () -> Unit,
    onShowSettings: () -> Unit,
    colors: CanvasColors
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        TextSectionHeader("PREFERÊNCIAS DO CANVAS", colors)

        CanvasToggle(label = "Modo Escuro", checked = isDarkMode, onCheckedChange = { onToggleTheme() })
        CanvasToggle(label = "Grade Visível", checked = isGridEnabled, onCheckedChange = { onToggleGrid() })
        CanvasToggle(label = "Bloquear Canvas", checked = isLocked, onCheckedChange = { onToggleLock() })

        Spacer(Modifier.height(12.dp))
        CanvasButton(
            text = "Configurações Globais",
            onClick = onShowSettings,
            variant = CanvasButtonVariant.Secondary,
            leadingIcon = Icons.Rounded.Settings,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TextSectionHeader(title: String, colors: CanvasColors) {
    Text(
        text = title,
        color = colors.accent,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
