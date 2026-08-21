package com.canvasstudio.ui.splash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasstudio.R
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SplashCenterEmblem(
    isTeenage: Boolean,
    colors: CanvasColors,
    fontFamily: FontFamily,
    trackingAnim: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .clip(if (isTeenage) RoundedCornerShape(8.dp) else CircleShape)
                    .border(
                        width = if (isTeenage) 2.dp else 1.5.dp,
                        brush = Brush.sweepGradient(listOf(colors.accent, colors.accentVariant, colors.accent.copy(alpha = 0.2f), colors.accent)),
                        shape = if (isTeenage) RoundedCornerShape(8.dp) else CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(colors.bgMenu, if (isTeenage) RoundedCornerShape(6.dp) else CircleShape)
                    .border(1.dp, colors.borderSubtle, if (isTeenage) RoundedCornerShape(6.dp) else CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, tint = colors.accent, modifier = Modifier.size(80.dp))
            }

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

        Text(
            text = "CANVAS STUDIO",
            color = colors.textMain,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = fontFamily,
            letterSpacing = trackingAnim.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val badgeText = if (isTeenage) "// TE-01 SYNTH ENGINE • READY" else "INFINITE WORKSPACE • REACTIVE UDF"
        Surface(
            color = colors.accent.copy(alpha = if (isTeenage) 0.15f else 0.10f),
            shape = if (isTeenage) RoundedCornerShape(3.dp) else RoundedCornerShape(999.dp),
            border = BorderStroke(if (isTeenage) 1.dp else 0.5.dp, colors.accent.copy(alpha = if (isTeenage) 0.5f else 0.25f)),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = badgeText,
                color = colors.accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
