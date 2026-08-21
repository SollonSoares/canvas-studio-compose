package com.canvasstudio.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.tokens.CanvasDimens

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

    val (bgColor, textColor) = when (variant) {
        CanvasBadgeVariant.Pix -> Pair(colors.badgePix.copy(alpha = 0.18f), colors.badgePix)
        CanvasBadgeVariant.Financial -> Pair(colors.success.copy(alpha = 0.15f), colors.success)
        CanvasBadgeVariant.Accent -> Pair(colors.accent.copy(alpha = 0.15f), colors.accent)
        CanvasBadgeVariant.Success -> Pair(colors.success.copy(alpha = 0.15f), colors.success)
        CanvasBadgeVariant.Danger -> Pair(colors.danger.copy(alpha = 0.15f), colors.danger)
        CanvasBadgeVariant.Neutral -> Pair(colors.bgInput, colors.textSecondary)
    }

    Surface(
        color = bgColor,
        shape = CanvasDimens.shapeXs,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}
