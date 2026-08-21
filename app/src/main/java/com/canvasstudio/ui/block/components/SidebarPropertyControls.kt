package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SidebarColorSelector(
    selectedColor: String?,
    onColorSelect: (String) -> Unit,
    colors: CanvasColors
) {
    val palette = listOf(
        "" to colors.textMain,
        "#FF3B30" to Color(0xFFFF3B30),
        "#FF9500" to Color(0xFFFF9500),
        "#34C759" to Color(0xFF34C759),
        "#007AFF" to Color(0xFF007AFF),
        "#AF52DE" to Color(0xFFAF52DE)
    )

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        palette.forEach { (hex, color) ->
            val isSel = (hex.isEmpty() && selectedColor.isNullOrEmpty()) || (hex == selectedColor)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(if (isSel) 2.dp else 0.dp, if (isSel) colors.accent else Color.Transparent, CircleShape)
                    .clickable { onColorSelect(hex) }
            )
        }
    }
}

@Composable
fun SidebarAlignmentSelector(
    currentAlign: String,
    onAlignSelect: (String) -> Unit,
    colors: CanvasColors
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
            onClick = { onAlignSelect("left") },
            modifier = Modifier.size(32.dp).background(if (currentAlign == "left") colors.accent.copy(0.15f) else Color.Transparent, RoundedCornerShape(4.dp))
        ) {
            Icon(Icons.Rounded.FormatAlignLeft, "Esquerda", tint = if (currentAlign == "left") colors.accent else colors.textMuted, modifier = Modifier.size(16.dp))
        }
        IconButton(
            onClick = { onAlignSelect("center") },
            modifier = Modifier.size(32.dp).background(if (currentAlign == "center") colors.accent.copy(0.15f) else Color.Transparent, RoundedCornerShape(4.dp))
        ) {
            Icon(Icons.Rounded.FormatAlignCenter, "Centro", tint = if (currentAlign == "center") colors.accent else colors.textMuted, modifier = Modifier.size(16.dp))
        }
        IconButton(
            onClick = { onAlignSelect("right") },
            modifier = Modifier.size(32.dp).background(if (currentAlign == "right") colors.accent.copy(0.15f) else Color.Transparent, RoundedCornerShape(4.dp))
        ) {
            Icon(Icons.Rounded.FormatAlignRight, "Direita", tint = if (currentAlign == "right") colors.accent else colors.textMuted, modifier = Modifier.size(16.dp))
        }
    }
}
