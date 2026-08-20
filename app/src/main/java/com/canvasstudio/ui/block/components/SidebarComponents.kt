package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun MenuSectionTitle(title: String, colors: CanvasColors) {
    Text(
        text = title,
        color = colors.textMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
fun ModuleToggle(
    label: String, 
    isEnabled: Boolean, 
    onToggle: (Boolean) -> Unit, 
    colors: CanvasColors
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), 
        horizontalArrangement = Arrangement.SpaceBetween, 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = colors.textMain, fontSize = 14.sp)
        Switch(
            checked = isEnabled, 
            onCheckedChange = onToggle, 
            colors = SwitchDefaults.colors(checkedThumbColor = colors.accent)
        )
    }
}
