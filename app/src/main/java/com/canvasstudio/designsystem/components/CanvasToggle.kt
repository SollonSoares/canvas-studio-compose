package com.canvasstudio.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.tokens.CanvasDimens

@Composable
fun CanvasToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    description: String? = null,
    activeColor: Color = CanvasTheme.colors.accent,
    enabled: Boolean = true
) {
    val colors = CanvasTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = CanvasDimens.spaceXs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label.isNotBlank() || !description.isNullOrBlank()) {
            Column(modifier = Modifier.weight(1f).padding(end = CanvasDimens.spaceSm)) {
                if (label.isNotBlank()) {
                    Text(
                        text = label,
                        color = if (enabled) colors.textMain else colors.textMuted,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.textMuted.copy(alpha = 0.35f)
            ),
            modifier = Modifier.scale(0.85f)
        )
    }
}
