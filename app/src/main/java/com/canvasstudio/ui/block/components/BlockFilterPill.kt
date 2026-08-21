package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun BoxScope.BlockFilterPill(
    appliedQuery: String,
    isEmptyResult: Boolean,
    onClearFilter: () -> Unit,
    colors: CanvasColors
) {
    if (appliedQuery.isEmpty()) return

    Column(
        Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = colors.accent.copy(alpha = 0.9f),
            shape = RoundedCornerShape(20.dp),
            elevation = 4.dp
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FilterList, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("Filtrando por: \"$appliedQuery\"", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClearFilter, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Limpar Busca", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }

        if (isEmptyResult) {
            Spacer(Modifier.height(100.dp))
            Text(
                "Nenhum bloco corresponde à sua busca.",
                color = colors.textMain.copy(0.4f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
