package com.canvasstudio.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.CanvasTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val colors = CanvasTheme.colors
    val isTeenage = CanvasTheme.isTeenage
    val fontFamily = CanvasTheme.fontFamily

    var startAnimation by remember { mutableStateOf(false) }
    var exiting by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = when {
            exiting -> 1.08f
            startAnimation -> 1f
            else -> 0.45f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val alphaAnim by animateFloatAsState(
        targetValue = when {
            exiting -> 0f
            startAnimation -> 1f
            else -> 0f
        },
        animationSpec = tween(durationMillis = if (exiting) 400 else 800, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(1400, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "glow"
    )

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
        SplashAmbientGrid(pulseGlow, alphaAnim, isTeenage, colors)

        Box(
            modifier = Modifier
                .scale(scaleAnim)
                .alpha(alphaAnim),
            contentAlignment = Alignment.Center
        ) {
            SplashCenterEmblem(isTeenage, colors, fontFamily, trackingAnim)
        }

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
