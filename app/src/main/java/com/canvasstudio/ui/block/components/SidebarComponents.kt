package com.canvasstudio.ui.block.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
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

@Composable
fun FontSizeSelector(
    currentSize: Int,
    onSizeSelected: (Int) -> Unit,
    colors: CanvasColors
) {
    val presets = listOf(11, 13, 16, 20, 24)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        presets.forEach { size ->
            val isSelected = currentSize == size
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) colors.accent else colors.bgInput)
                    .border(
                        1.dp,
                        if (isSelected) colors.accent else colors.borderSubtle,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onSizeSelected(size) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${size}",
                    color = if (isSelected) Color.White else colors.textMain,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
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
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Negrito
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isBold) colors.accent.copy(alpha = 0.2f) else colors.bgInput)
                .border(
                    1.dp,
                    if (isBold) colors.accent else colors.borderSubtle,
                    RoundedCornerShape(6.dp)
                )
                .clickable { onToggleBold() }
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "B",
                    color = if (isBold) colors.accent else colors.textMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Negrito",
                    color = if (isBold) colors.accent else colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Itálico
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isItalic) colors.accent.copy(alpha = 0.2f) else colors.bgInput)
                .border(
                    1.dp,
                    if (isItalic) colors.accent else colors.borderSubtle,
                    RoundedCornerShape(6.dp)
                )
                .clickable { onToggleItalic() }
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "I",
                    color = if (isItalic) colors.accent else colors.textMain,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Itálico",
                    color = if (isItalic) colors.accent else colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TextAlignSelector(
    currentAlign: String,
    onAlignSelected: (String) -> Unit,
    colors: CanvasColors
) {
    val options = listOf(
        Triple("left", Icons.Rounded.FormatAlignLeft, "Esquerda"),
        Triple("center", Icons.Rounded.FormatAlignCenter, "Centro"),
        Triple("right", Icons.Rounded.FormatAlignRight, "Direita")
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (alignKey, icon, _) ->
            val isSelected = currentAlign.lowercase() == alignKey
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) colors.accent.copy(alpha = 0.25f) else colors.bgInput)
                    .border(
                        1.dp,
                        if (isSelected) colors.accent else colors.borderSubtle,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onAlignSelected(alignKey) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = alignKey,
                    tint = if (isSelected) colors.accent else colors.textMain,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TextColorPalette(
    currentColorHex: String?,
    onColorSelected: (String) -> Unit,
    colors: CanvasColors
) {
    val colorPresets = listOf(
        Pair("", colors.textMain),                  // Padrão
        Pair("#0A84FF", Color(0xFF0A84FF)),        // Azul Ninja
        Pair("#FFD60A", Color(0xFFFFD60A)),        // Dourado Chakra
        Pair("#FF453A", Color(0xFFFF453A)),        // Vermelho Dano
        Pair("#30D158", Color(0xFF30D158)),        // Verde Cura
        Pair("#BF5AF2", Color(0xFFBF5AF2)),        // Roxo Jutsu
        Pair("#FF9F0A", Color(0xFFFF9F0A))         // Laranja Selo
    )

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        colorPresets.forEach { (hex, colorVal) ->
            val isSelected = (currentColorHex ?: "") == hex
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(colorVal)
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) colors.accent else Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
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
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = colors.textMain, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "%.1f".format(java.util.Locale.US, value),
                color = colors.accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = colors.accent,
                activeTrackColor = colors.accent,
                inactiveTrackColor = colors.borderSubtle
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}

