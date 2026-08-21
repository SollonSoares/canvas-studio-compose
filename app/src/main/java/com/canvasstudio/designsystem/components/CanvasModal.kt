package com.canvasstudio.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.tokens.CanvasDimens

@Composable
fun CanvasModal(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CanvasTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 620.dp)
                .padding(vertical = CanvasDimens.spaceMd),
            shape = CanvasDimens.shapeXl,
            color = colors.bgMenu,
            elevation = 8.dp,
            border = BorderStroke(1.dp, colors.borderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CanvasDimens.spaceLg)
            ) {
                // 1. Cabeçalho Fixo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = colors.textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Fechar",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(CanvasDimens.spaceSm))

                // 2. Área de Conteúdo com Scroll Independente
                val scrollState = rememberScrollState()
                val scrollModifier = if (scrollable) {
                    Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                } else {
                    Modifier.weight(1f, fill = false)
                }

                Column(
                    modifier = scrollModifier
                        .fillMaxWidth()
                        .padding(bottom = CanvasDimens.spaceSm)
                ) {
                    content()
                }

                // 3. Rodapé Fixo (Ações ancoradas - Nunca sobrepõem o conteúdo)
                if (confirmButton != null || dismissButton != null) {
                    Spacer(Modifier.height(CanvasDimens.spaceSm))
                    Divider(color = colors.borderSubtle, thickness = 1.dp)
                    Spacer(Modifier.height(CanvasDimens.spaceMd))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismissButton != null) {
                            dismissButton()
                            Spacer(Modifier.width(CanvasDimens.spaceSm))
                        }
                        if (confirmButton != null) {
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CanvasSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val colors = CanvasTheme.colors
    Text(
        text = title.uppercase(),
        color = colors.textMuted,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        modifier = modifier.padding(vertical = CanvasDimens.spaceXs)
    )
}

@Composable
fun CanvasDivider(
    modifier: Modifier = Modifier
) {
    val colors = CanvasTheme.colors
    Divider(
        color = colors.borderSubtle,
        thickness = 1.dp,
        modifier = modifier.padding(vertical = CanvasDimens.spaceSm)
    )
}
