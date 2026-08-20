package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.ui.block.utils.parseRichText
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun CanvasTable(headers: List<String>, rows: List<List<String>>, colors: CanvasColors) {
    // Scroll horizontal independente para tabelas, assim como no Web
    Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .widthIn(min = 200.dp)
                .border(1.dp, colors.borderSubtle)
        ) {
            if (headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.accent.copy(alpha = 0.15f))
                        .padding(vertical = 10.dp, horizontal = 12.dp)
                ) {
                    headers.forEach { header ->
                        Text(
                            text = header.uppercase(),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            color = colors.accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) Color.Transparent else colors.textMain.copy(alpha = 0.03f))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { cell ->
                        Text(
                            text = parseRichText(cell, 11),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            color = colors.textMain.copy(0.9f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
