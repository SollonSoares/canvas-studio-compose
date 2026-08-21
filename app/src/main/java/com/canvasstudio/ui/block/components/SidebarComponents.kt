package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun FontSizeSelector(
    currentSize: Int,
    onSizeSelected: (Int) -> Unit,
    colors: CanvasColors
) {
    val presets = listOf(11, 13, 16, 20, 24)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        presets.forEach { size ->
            val isSelected = currentSize == size
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) colors.accent else colors.bgInput)
                    .border(1.dp, if (isSelected) colors.accent else colors.borderSubtle, RoundedCornerShape(6.dp))
                    .clickable { onSizeSelected(size) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$size", color = if (isSelected) Color.White else colors.textMain, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

@Composable
fun TextStyleToggle(
    isBold: Boolean,
    onToggleBold: () -> Unit,
    isItalic: Boolean,
    onToggleItalic: () -> Unit,
    colors: CanvasColors
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isBold) colors.accent.copy(alpha = 0.2f) else colors.bgInput)
                .border(1.dp, if (isBold) colors.accent else colors.borderSubtle, RoundedCornerShape(6.dp))
                .clickable { onToggleBold() }
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("B  Negrito", color = if (isBold) colors.accent else colors.textMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isItalic) colors.accent.copy(alpha = 0.2f) else colors.bgInput)
                .border(1.dp, if (isItalic) colors.accent else colors.borderSubtle, RoundedCornerShape(6.dp))
                .clickable { onToggleItalic() }
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("I  Itálico", color = if (isItalic) colors.accent else colors.textMain, fontSize = 11.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttributeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    colors: CanvasColors
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = colors.textMain, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("%.1f".format(java.util.Locale.US, value), color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 19,
            colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent, inactiveTrackColor = colors.borderSubtle),
            modifier = Modifier.height(26.dp)
        )
    }
}
