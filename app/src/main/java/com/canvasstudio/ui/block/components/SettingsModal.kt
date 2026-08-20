package com.canvasstudio.ui.block.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SettingsModal(
    title: String, 
    onTitleChange: (String) -> Unit, 
    canvasDimensions: Pair<Int, Int>,
    onDimensionsChange: (Int, Int) -> Unit,
    modules: Map<String, Boolean>, 
    onToggleModule: (String, Boolean) -> Unit, 
    onDismiss: () -> Unit, 
    colors: CanvasColors
) {
    val presets = listOf(
        Triple("Square (Std)", 2000, 2000),
        Triple("Full HD", 1920, 1080),
        Triple("4K Ultra", 3840, 2160),
        Triple("Mobile", 1080, 1920)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = colors.bgMenu,
        shape = RoundedCornerShape(14.dp), // radius-lg: 14px
        title = { Text("Configurações", color = colors.textMain, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Título do Projeto", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                TextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colors.textMain, 
                        cursorColor = colors.accent, 
                        focusedIndicatorColor = colors.accent, 
                        backgroundColor = Color.Transparent
                    )
                )
                
                Spacer(Modifier.height(24.dp))

                Text("Dimensões do Canvas", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                
                presets.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (name, w, h) ->
                            val isSelected = canvasDimensions.first == w && canvasDimensions.second == h
                            OutlinedButton(
                                onClick = { onDimensionsChange(w, h) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.border),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = if (isSelected) colors.accent.copy(alpha = 0.1f) else Color.Transparent
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(name, color = if (isSelected) colors.accent else colors.textMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${w}x${h}", color = if (isSelected) colors.accent.copy(0.7f) else colors.textMain.copy(0.5f), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(16.dp))
                
                Text("Módulos Habilitados", color = colors.textMain.copy(0.6f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                modules.forEach { (type, enabled) ->
                    ModuleToggle(type.replaceFirstChar { it.uppercase() }, enabled, { onToggleModule(type, it) }, colors)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(backgroundColor = colors.accent)) {
                Text("Fechar", color = Color.White)
            }
        }
    )
}
