package com.canvasstudio.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.tokens.CanvasDimens

enum class CanvasButtonVariant {
    Primary,
    Secondary,
    Outlined,
    Ghost,
    Danger
}

@Composable
fun CanvasButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: CanvasButtonVariant = CanvasButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    shape: Shape = CanvasDimens.shapeMd
) {
    CanvasButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        enabled = enabled,
        shape = shape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CanvasButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: CanvasButtonVariant = CanvasButtonVariant.Primary,
    enabled: Boolean = true,
    shape: Shape = CanvasDimens.shapeMd,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val colors = CanvasTheme.colors

    when (variant) {
        CanvasButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colors.accent,
                    contentColor = Color.White
                ),
                contentPadding = contentPadding,
                content = content
            )
        }
        CanvasButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colors.bgButton,
                    contentColor = colors.textMain
                ),
                contentPadding = contentPadding,
                content = content
            )
        }
        CanvasButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                border = BorderStroke(1.dp, colors.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = colors.textMain
                ),
                contentPadding = contentPadding,
                content = content
            )
        }
        CanvasButtonVariant.Ghost -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.accent
                ),
                contentPadding = contentPadding,
                content = content
            )
        }
        CanvasButtonVariant.Danger -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colors.danger,
                    contentColor = Color.White
                ),
                contentPadding = contentPadding,
                content = content
            )
        }
    }
}
