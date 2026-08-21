package com.canvasstudio.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.R
import com.canvasstudio.designsystem.CanvasTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val colors = CanvasTheme.colors
    val isTeenage = CanvasTheme.isTeenage
    val fontFamily = CanvasTheme.fontFamily

    var startAnimation by remember { mutableStateOf(false) }
    var exiting by remember { mutableStateOf(false) }

    // 1. Animação de Escala com Física de Mola
    val scaleAnim by animateFloatAsState(
        targetValue = when {
            exiting -> 1.08f
            startAnimation -> 1f
            else -> 0.45f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // 2. Animação de Opacidade (Fade In e Fade Out Suave de Saída)
    val alphaAnim by animateFloatAsState(
        targetValue = when {
            exiting -> 0f
            startAnimation -> 1f
            else -> 0f
        },
        animationSpec = tween(durationMillis = if (exiting) 400 else 800, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    // 3. Efeito de Respiração Luminosa Contínua (Pulse Glow)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // 4. Letter-Spacing Dinâmico (Expandindo com elegância)
    val trackingAnim by animateFloatAsState(
        targetValue = if (startAnimation) 3.5f else 0.5f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "tracking"
    )

    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
        delay(2100)
        exiting = true
        delay(400)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain),
        contentAlignment = Alignment.Center
    ) {
        // Halo de Luz Ambiente
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(pulseGlow)
                .alpha(alphaAnim * (if (isTeenage) 0.18f else 0.28f))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.accent, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // Grade de Fundo Reativa
        Canvas(modifier = Modifier.fillMaxSize().alpha(alphaAnim * 0.22f)) {
            val step = 28.dp.toPx()
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    if (isTeenage) {
                        drawLine(
                            color = colors.accent.copy(alpha = 0.35f),
                            start = Offset(x - 3.dp.toPx(), y),
                            end = Offset(x + 3.dp.toPx(), y),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = colors.accent.copy(alpha = 0.35f),
                            start = Offset(x, y - 3.dp.toPx()),
                            end = Offset(x, y + 3.dp.toPx()),
                            strokeWidth = 1.dp.toPx()
                        )
                    } else {
                        drawCircle(
                            color = colors.accent.copy(alpha = 0.3f),
                            radius = 1.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                    y += step
                }
                x += step
            }
        }

        // Conteúdo Central
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scaleAnim)
                .alpha(alphaAnim)
        ) {
            // Emblema Central
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
            ) {
                // Anel de Borda Exterior com Gradiente Rotativo
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(if (isTeenage) RoundedCornerShape(8.dp) else CircleShape)
                        .border(
                            width = if (isTeenage) 2.dp else 1.5.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    colors.accent,
                                    colors.accentVariant,
                                    colors.accent.copy(alpha = 0.2f),
                                    colors.accent
                                )
                            ),
                            shape = if (isTeenage) RoundedCornerShape(8.dp) else CircleShape
                        )
                )

                // Superfície Interna do Logo
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            color = colors.bgMenu,
                            shape = if (isTeenage) RoundedCornerShape(6.dp) else CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = colors.borderSubtle,
                            shape = if (isTeenage) RoundedCornerShape(6.dp) else CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(80.dp)
                    )
                }

                // LED indicador no estilo Teenage Engineering
                if (isTeenage) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(7.dp)
                            .background(colors.accent, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título com Expansão Tipográfica
            Text(
                text = "CANVAS STUDIO",
                color = colors.textMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = fontFamily,
                letterSpacing = trackingAnim.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badge Dinâmico do Motor Ativo
            val systemBadgeText = if (isTeenage) {
                "// TE-01 SYNTH ENGINE • READY"
            } else {
                "INFINITE WORKSPACE • REACTIVE UDF"
            }

            Surface(
                color = colors.accent.copy(alpha = if (isTeenage) 0.15f else 0.10f),
                shape = if (isTeenage) RoundedCornerShape(3.dp) else RoundedCornerShape(999.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isTeenage) 1.dp else 0.5.dp,
                    color = colors.accent.copy(alpha = if (isTeenage) 0.5f else 0.25f)
                ),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = systemBadgeText,
                    color = colors.accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Rodapé de Versão e Arquitetura
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(alphaAnim * 0.6f)
        ) {
            Text(
                text = if (isTeenage) "SYS // v2.0 • HARDWARE ARCHITECTURE" else "v2.0 • Native Compose Architecture",
                color = colors.textMuted,
                fontSize = 10.5.sp,
                fontFamily = fontFamily,
                letterSpacing = 0.5.sp
            )
        }
    }
}
