package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canvasstudio.designsystem.components.CanvasButton
import com.canvasstudio.designsystem.components.CanvasButtonVariant
import com.canvasstudio.designsystem.components.CanvasTextField
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SidebarSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    colors: CanvasColors
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CanvasTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Buscar blocos...",
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange(""); onSearch() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, "Limpar", tint = colors.textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
            CanvasButton(
                text = "Buscar",
                onClick = onSearch,
                variant = CanvasButtonVariant.Primary,
                leadingIcon = Icons.Default.Search
            )
        }
    }
}
