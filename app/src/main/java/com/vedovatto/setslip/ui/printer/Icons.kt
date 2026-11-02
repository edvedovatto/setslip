package com.vedovatto.setslip.ui.printer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

fun DrawScope.gearIcon(color: Color) {
    val u = min(size.width, size.height)
    val c = center
    val ring = u * 0.2f
    drawCircle(color, radius = u * 0.26f, center = c, style = Stroke(width = ring))
    repeat(8) { i ->
        rotate(i * 45f, pivot = c) {
            drawRoundRect(
                color,
                topLeft = Offset(c.x - u * 0.07f, c.y - u * 0.46f),
                size = Size(u * 0.14f, u * 0.2f),
                cornerRadius = CornerRadius(u * 0.03f),
            )
        }
    }
}

fun DrawScope.pileIcon(color: Color) {
    val u = min(size.width, size.height)
    val w = u * 0.62f
    val h = u * 0.4f
    val stroke = Stroke(width = u * 0.09f, join = StrokeJoin.Round)
    for (i in 0..2) {
        val dy = (2 - i) * u * 0.2f
        drawRoundRect(
            color,
            topLeft = Offset((size.width - w) / 2f, size.height * 0.5f - h / 2f - u * 0.2f + dy - (2 - i) * 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(u * 0.06f),
            style = stroke,
            alpha = 0.5f + 0.25f * i,
        )
    }
}

fun DrawScope.starIcon(color: Color, filled: Boolean) {
    val u = min(size.width, size.height)
    val c = center
    val outer = u * 0.46f
    val inner = outer * 0.42f
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = Math.toRadians((i * 36 - 90).toDouble())
        val p = Offset(c.x + (r * cos(a)).toFloat(), c.y + (r * sin(a)).toFloat())
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    path.close()
    if (filled) drawPath(path, color)
    else drawPath(path, color, style = Stroke(width = u * 0.09f, join = StrokeJoin.Round))
}

fun DrawScope.backIcon(color: Color) {
    val u = min(size.width, size.height)
    val c = center
    val path = Path().apply {
        moveTo(c.x + u * 0.16f, c.y - u * 0.3f)
        lineTo(c.x - u * 0.16f, c.y)
        lineTo(c.x + u * 0.16f, c.y + u * 0.3f)
    }
    drawPath(path, color, style = Stroke(width = u * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

fun DrawScope.checkIcon(color: Color, width: Float) {
    val u = min(size.width, size.height)
    val path = Path().apply {
        moveTo(size.width * 0.14f, size.height * 0.56f)
        lineTo(size.width * 0.4f, size.height * 0.84f)
        lineTo(size.width * 0.95f, size.height * 0.04f)
    }
    drawPath(path, color, style = Stroke(width = width.coerceAtLeast(u * 0.1f), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

fun DrawScope.refreshIcon(color: Color) {
    val u = min(size.width, size.height)
    val c = center
    val r = u * 0.34f
    val w = u * 0.12f
    drawArc(
        color,
        startAngle = -60f,
        sweepAngle = 290f,
        useCenter = false,
        topLeft = Offset(c.x - r, c.y - r),
        size = Size(r * 2, r * 2),
        style = Stroke(width = w, cap = StrokeCap.Round),
    )
    val a = Math.toRadians(-60.0)
    val tip = Offset(c.x + (r * cos(a)).toFloat(), c.y + (r * sin(a)).toFloat())
    val head = Path().apply {
        moveTo(tip.x - u * 0.2f, tip.y - u * 0.02f)
        lineTo(tip.x + u * 0.12f, tip.y - u * 0.18f)
        lineTo(tip.x + u * 0.16f, tip.y + u * 0.16f)
        close()
    }
    drawPath(head, color)
}

fun DrawScope.shareIcon(color: Color) {
    val k = min(size.width, size.height) / 24f
    val w = 1.7f * k
    for ((x, y) in listOf(6f to 12f, 17.5f to 6f, 17.5f to 18f)) {
        drawCircle(color, radius = 2.3f * k, center = Offset(x * k, y * k), style = Stroke(width = w))
    }
    drawLine(color, Offset(8f * k, 11f * k), Offset(15.5f * k, 7f * k), strokeWidth = w, cap = StrokeCap.Round)
    drawLine(color, Offset(8f * k, 13f * k), Offset(15.5f * k, 17f * k), strokeWidth = w, cap = StrokeCap.Round)
}

fun DrawScope.helpIcon(color: Color) {
    val k = min(size.width, size.height) / 24f
    val w = 1.7f * k
    drawCircle(color, radius = 8.5f * k, center = center, style = Stroke(width = w))
    drawArc(
        color, startAngle = 180f, sweepAngle = 235f, useCenter = false,
        topLeft = Offset(9.6f * k, 7.2f * k), size = Size(4.8f * k, 4.8f * k),
        style = Stroke(width = w, cap = StrokeCap.Round),
    )
    val tail = Path().apply {
        moveTo(13.4f * k, 11.6f * k)
        quadraticTo(12f * k, 12.4f * k, 12f * k, 13.9f * k)
    }
    drawPath(tail, color, style = Stroke(width = w, cap = StrokeCap.Round))
    drawCircle(color, radius = 0.8f * k, center = Offset(12f * k, 16.8f * k))
}

private fun DrawScope.svgStroke(d: String, color: Color) {
    val k = min(size.width, size.height) / 24f
    val path = androidx.compose.ui.graphics.vector.PathParser().parsePathString(d).toPath()
    scale(k, k, pivot = Offset.Zero) {
        drawPath(path, color, style = Stroke(width = 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

fun DrawScope.handIcon(color: Color) = svgStroke(
    "M8 13V6.5a1.5 1.5 0 0 1 3 0V12M11 11V5a1.5 1.5 0 0 1 3 0v6.5M14 11V7a1.5 1.5 0 0 1 3 0v8c0 3-2 5-5 5s-4-1.5-5-4l-2-3.5a1.4 1.4 0 0 1 2.3-1.5L8 13",
    color,
)

fun DrawScope.arrowDownIcon(color: Color) = svgStroke("M12 4v15M6 13l6 6 6-6", color)
