package com.canvasstudio.ui.block.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.theme.CanvasTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SidebarButton(
    label: String, 
    icon: ImageVector, 
    color: Color, 
    isDanger: Boolean = false, 
    onClick: () -> Unit
) {
    val colors = CanvasTheme.colors
    val bgColor = if (isDanger) colors.danger.copy(alpha = 0.15f) else colors.bgButton
    val contentColor = if (isDanger) colors.danger else color

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp),
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        border = if (!isDanger) BorderStroke(1.dp, colors.borderSubtle) else BorderStroke(1.dp, colors.danger.copy(0.2f))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
