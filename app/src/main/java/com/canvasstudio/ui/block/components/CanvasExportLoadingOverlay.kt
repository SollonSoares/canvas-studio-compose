package com.canvasstudio.ui.block.components

import androidx.compose.foundation.background
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.components.CanvasCard
import com.canvasstudio.designsystem.tokens.CanvasColors

@Composable
fun CanvasExportLoadingOverlay(
    colors: CanvasColors,
    modifier: Modifier = Modifier,
    title: String = "Sincronizando Galeria",
    description: String = "Exportando imagens e atualizando os links no Canvas...\nPor favor, aguarde o término do processo."
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .pointerInput(Unit) {
                // Intercepta todos os gestos e toques na tela enquanto o loading estiver ativo
            },
        contentAlignment = Alignment.Center
    ) {
        CanvasCard(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = 380.dp),
            backgroundColor = colors.bgCard,
            borderColor = colors.border,
            borderWidth = 1.dp,
            elevation = 12.dp,
            shape = RoundedCornerShape(20.dp),
            contentPadding = 24.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    color = colors.accent,
                    strokeWidth = 3.5.dp,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
