package com.canvasstudio.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.CanvasTheme

enum class CanvasBadgeVariant {
    Pix,
    Financial,
    Accent,
    Success,
    Danger,
    Neutral
}

@Composable
fun CanvasBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: CanvasBadgeVariant = CanvasBadgeVariant.Neutral
) {
    val colors = CanvasTheme.colors
    val isTeenage = CanvasTheme.isTeenage

    val (bgColor, textColor) = when (variant) {
        CanvasBadgeVariant.Pix -> Pair(colors.badgePix.copy(alpha = if (isTeenage) 0.25f else 0.18f), colors.badgePix)
        CanvasBadgeVariant.Financial -> Pair(colors.success.copy(alpha = if (isTeenage) 0.22f else 0.15f), colors.success)
        CanvasBadgeVariant.Accent -> Pair(colors.accent.copy(alpha = if (isTeenage) 0.22f else 0.15f), colors.accent)
        CanvasBadgeVariant.Success -> Pair(colors.success.copy(alpha = if (isTeenage) 0.22f else 0.15f), colors.success)
        CanvasBadgeVariant.Danger -> Pair(colors.danger.copy(alpha = if (isTeenage) 0.22f else 0.15f), colors.danger)
        CanvasBadgeVariant.Neutral -> Pair(colors.bgInput, colors.textSecondary)
    }

    val shape = if (isTeenage) RoundedCornerShape(2.dp) else RoundedCornerShape(999.dp)
    val border = if (isTeenage) BorderStroke(1.dp, textColor.copy(alpha = 0.55f)) else BorderStroke(0.5.dp, textColor.copy(alpha = 0.2f))
    val displayText = if (isTeenage) "[ $text ]" else text

    Surface(
        color = bgColor,
        shape = shape,
        border = border,
        modifier = modifier
    ) {
        Text(
            text = displayText,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CanvasTheme.fontFamily,
            letterSpacing = if (isTeenage) 0.4.sp else 0.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
