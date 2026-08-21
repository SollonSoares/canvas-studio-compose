package com.canvasstudio.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CanvasShapes(
    val shapeXs: Shape,
    val shapeSm: Shape,
    val shapeMd: Shape,
    val shapeLg: Shape,
    val shapeXl: Shape,
    val shapeFull: Shape,
    val borderWidth: Dp,
    val isMonospace: Boolean,
    val isTeenage: Boolean
)

val CupertinoShapes = CanvasShapes(
    shapeXs = RoundedCornerShape(6.dp),
    shapeSm = RoundedCornerShape(10.dp),
    shapeMd = RoundedCornerShape(14.dp),
    shapeLg = RoundedCornerShape(18.dp),
    shapeXl = RoundedCornerShape(24.dp),
    shapeFull = RoundedCornerShape(999.dp),
    borderWidth = 1.dp,
    isMonospace = false,
    isTeenage = false
)

val TeenageShapes = CanvasShapes(
    shapeXs = RoundedCornerShape(2.dp),
    shapeSm = RoundedCornerShape(3.dp),
    shapeMd = RoundedCornerShape(4.dp),
    shapeLg = RoundedCornerShape(6.dp),
    shapeXl = RoundedCornerShape(8.dp),
    shapeFull = RoundedCornerShape(3.dp),
    borderWidth = 1.5.dp,
    isMonospace = true,
    isTeenage = true
)

object CanvasDimens {
    // Spacing tokens
    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val spaceLg = 16.dp
    val spaceXl = 24.dp
    val space2xl = 32.dp

    // Corner Radii Default
    val radiusXs = 4.dp
    val radiusSm = 6.dp
    val radiusMd = 8.dp
    val radiusLg = 12.dp
    val radiusXl = 14.dp
    val radius2xl = 18.dp
    val radiusFull = 999.dp

    // Fallback Static Shapes
    val shapeXs = RoundedCornerShape(radiusXs)
    val shapeSm = RoundedCornerShape(radiusSm)
    val shapeMd = RoundedCornerShape(radiusMd)
    val shapeLg = RoundedCornerShape(radiusLg)
    val shapeXl = RoundedCornerShape(radiusXl)
    val shape2xl = RoundedCornerShape(radius2xl)
    val shapeFull = RoundedCornerShape(radiusFull)

    // Touch Targets
    val touchTargetMin = 48.dp
    val touchTargetIcon = 36.dp
    val touchTargetBadge = 24.dp
}

object CanvasTypography {
    val h1 = 20.sp
    val h2 = 18.sp
    val h3 = 16.sp
    val bodyLg = 14.sp
    val body = 13.sp
    val bodySm = 12.sp
    val caption = 11.sp
    val badge = 10.sp
    val micro = 9.sp
}
