package com.vedovatto.setslip.ui.printer

import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Random

fun Modifier.softShadow(
    color: Color,
    blur: Dp,
    corner: Dp = 0.dp,
    spread: Dp = 0.dp,
    offsetY: () -> Dp = { 0.dp },
    alpha: () -> Float = { 1f },
): Modifier = drawWithCache {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.TRANSPARENT }
    val radius = (blur.toPx() * 0.87f).coerceAtLeast(0.1f)
    val r = corner.toPx()
    val s = spread.toPx()
    onDrawBehind {
        val a = alpha()
        if (a <= 0f) return@onDrawBehind
        val shadow = color.copy(alpha = (color.alpha * a).coerceAtMost(0.996f))
        paint.setShadowLayer(radius, 0f, offsetY().toPx(), shadow.toArgb())
        drawIntoCanvas {
            it.nativeCanvas.drawRoundRect(-s, -s, size.width + s, size.height + s, r + s, r + s, paint)
        }
    }
}

fun DrawScope.scanlines(color: Color, step: Dp = 3.dp, thickness: Dp = 1.dp) {
    val stepPx = step.toPx()
    val h = thickness.toPx()
    var y = 0f
    while (y < size.height) {
        drawRect(color, topLeft = Offset(0f, y), size = androidx.compose.ui.geometry.Size(size.width, h))
        y += stepPx
    }
}

fun DrawScope.hairline(y: Float, color: Color, dash: Dp, gap: Dp) = drawLine(
    color = color,
    start = Offset(0f, y),
    end = Offset(size.width, y),
    strokeWidth = 1.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash.toPx(), gap.toPx())),
)

@Composable
fun rememberPaperGrain(): ShaderBrush = remember {
    val n = 96
    val bitmap = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
    val rnd = Random(7)
    for (x in 0 until n) for (y in 0 until n) {
        val f = rnd.nextFloat()
        val a = (f * f * f * 34).toInt()
        val c = if (rnd.nextBoolean()) android.graphics.Color.argb(a, 60, 48, 30)
        else android.graphics.Color.argb(a / 2, 255, 255, 255)
        bitmap.setPixel(x, y, c)
    }
    ShaderBrush(ImageShader(bitmap.asImageBitmap(), TileMode.Repeated, TileMode.Repeated))
}

@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}

class Haptics(private val view: View, private val enabled: () -> Boolean) {
    fun feedStep() = fire(HapticFeedbackConstants.CLOCK_TICK)
    fun key() = fire(HapticFeedbackConstants.KEYBOARD_TAP)
    fun hold() = fire(HapticFeedbackConstants.LONG_PRESS)
    fun reject() = fire(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
        else HapticFeedbackConstants.LONG_PRESS,
    )

    private fun fire(constant: Int) {
        if (enabled()) view.performHapticFeedback(constant)
    }
}

@Composable
fun rememberHaptics(enabled: Boolean): Haptics {
    val view = LocalView.current
    val current by rememberUpdatedState(enabled)
    return remember(view) { Haptics(view) { current } }
}
