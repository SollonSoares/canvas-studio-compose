package com.canvasstudio.ui.block.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun BoxScope.BlockFloatingZoomBar(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onResetVision: () -> Unit,
    colors: CanvasColors
) {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = 16.dp),
        color = colors.bgCard.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp),
        elevation = 6.dp,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onScaleChange((scale / 1.3f).coerceIn(0.05f, 5.0f)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Rounded.Remove, "Zoom Out", tint = colors.textMain, modifier = Modifier.size(16.dp))
            }
            Text(
                text = "${(scale * 100).toInt()}%",
                color = colors.textMain,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onResetVision() }
                    .padding(horizontal = 6.dp)
            )
            IconButton(
                onClick = { onScaleChange((scale * 1.3f).coerceIn(0.05f, 5.0f)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Rounded.Add, "Zoom In", tint = colors.textMain, modifier = Modifier.size(16.dp))
            }
            Box(Modifier.height(16.dp).width(1.dp).background(colors.borderSubtle))
            IconButton(
                onClick = onResetVision,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Rounded.CenterFocusStrong, "Centralizar Workspace", tint = colors.accent, modifier = Modifier.size(16.dp))
            }
        }
    }
}
