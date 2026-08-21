package com.canvasstudio.ui.block.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun BlockScreenTopBar(
    brandTitle: String,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onResetVision: () -> Unit,
    colors: CanvasColors
) {
    TopAppBar(
        title = { Text(brandTitle, color = colors.textMain, fontWeight = FontWeight.Bold) },
        backgroundColor = colors.bgMenu,
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, "Configurações", tint = colors.accent)
            }
            IconButton(onClick = onResetVision) {
                Icon(Icons.Default.FilterCenterFocus, "Resetar Visão", tint = colors.accent)
            }
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, "Menu", tint = colors.accent)
            }
        }
    )
}
