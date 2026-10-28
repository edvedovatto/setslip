package com.vedovatto.setslip.ui.printer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vedovatto.setslip.domain.Workout
import com.vedovatto.setslip.ui.printer.PrinterTimings.DROP_MS
import com.vedovatto.setslip.ui.printer.PrinterTimings.FEED_MS
import com.vedovatto.setslip.ui.printer.PrinterTimings.IMPULSE_MS
import com.vedovatto.setslip.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val HANDOFF_MS = 100

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PrinterScreen(vm: PrinterViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val motion = rememberMotionEnabled()
    val haptics = rememberHaptics()
    var feeding by remember { mutableStateOf(false) }
    val sounds = rememberPrinterSounds()
    LaunchedEffect(feeding, state.soundOn) {
        if (feeding && state.soundOn) sounds.playFeed() else sounds.stopFeed()
    }
    LaunchedEffect(state.discarding?.serial) {
        if (state.discarding != null && state.soundOn && motion) sounds.playDrop()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.Graphite)
            .semantics {
                testTagsAsResourceId = true
                stateDescription = state.phase.description
            }
            .testTag("printer"),
    ) {
        PrinterHeader(
            printing = feeding,
            soundOn = state.soundOn,
            onToggleSound = vm::onToggleSound,
            modifier = Modifier.zIndex(2f),
        )
        PaperTray(
            state = state,
            motion = motion,
            haptics = haptics,
            onFeeding = { feeding = it },
            onFinished = vm::onPrintFinished,
            modifier = Modifier
                .weight(1f)
                .zIndex(1f),
        )
        KeyPanel(
            state = state,
            haptics = haptics,
            onKey = vm::onKey,
            onPrint = vm::onPrint,
            onReprint = vm::onReprint,
            modifier = Modifier.zIndex(3f),
        )
    }
}

@Composable
private fun PaperTray(
    state: PrinterUiState,
    motion: Boolean,
    haptics: Haptics,
    onFeeding: (Boolean) -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier,
) {
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .clipToBounds()
            .trayBackground(),
    ) {
        val trayHeight = constraints.maxHeight.toFloat()
        state.discarding?.let { w ->
            key(w.serial) { DroppingPaper(w, trayHeight, motion) }
        }
        state.current?.let { w ->
            key(w.serial) {
                FeedingPaper(
                    workout = w,
                    animate = state.busy,
                    delayMs = if (state.discarding != null) DROP_MS + HANDOFF_MS else 0,
                    motion = motion,
                    haptics = haptics,
                    onFeeding = onFeeding,
                    onFinished = onFinished,
                )
            }
        }
        Sink(Modifier.align(Alignment.BottomCenter))
    }
}

private val PaperModifier = Modifier
    .padding(horizontal = 30.dp)
    .fillMaxWidth()

@Composable
private fun FeedingPaper(
    workout: Workout,
    animate: Boolean,
    delayMs: Int,
    motion: Boolean,
    haptics: Haptics,
    onFeeding: (Boolean) -> Unit,
    onFinished: () -> Unit,
) {
    val density = LocalDensity.current
    val restPx = with(density) { PrinterTimings.restOffsetFor(workout).toPx() }
    val jitterPx = with(density) { PrinterTimings.jitter.toPx() }
    val shouldAnimate = remember { animate }
    var heightPx by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(!shouldAnimate) }
    var jitter by remember { mutableStateOf(false) }
    val y = remember { Animatable(restPx) }
    val glow = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (!shouldAnimate) return@LaunchedEffect
        val h = snapshotFlow { heightPx }.first { it > 0 }.toFloat()
        y.snapTo(-PrinterTimings.feedSteps.first().second * h)
        visible = true
        if (!motion) {
            y.snapTo(restPx)
            onFinished()
            return@LaunchedEffect
        }
        delay(delayMs.toLong())
        onFeeding(true)
        launch { glow.animateTo(1f, tween(400)) }
        val motor = launch {
            while (true) {
                jitter = !jitter
                delay(PrinterTimings.JITTER_HALF_PERIOD_MS)
            }
        }
        val ticks = launch {
            var elapsed = 0
            PrinterTimings.feedSteps.windowed(2).filter { (a, b) -> a.second != b.second }.forEach { (a, _) ->
                delay((a.first - elapsed).toLong())
                elapsed = a.first
                haptics.feedStep()
            }
        }
        y.animateTo(
            restPx,
            keyframes {
                durationMillis = FEED_MS
                PrinterTimings.feedSteps.dropLast(1).forEach { (t, f) ->
                    -f * h at t using PrinterTimings.feedEasing
                }
            },
        )
        motor.cancel()
        ticks.cancel()
        jitter = false
        onFeeding(false)
        onFinished()
        glow.animateTo(0f, tween(400))
    }

    PaperSlip(
        workout = workout,
        glow = { glow.value },
        modifier = PaperModifier
            .onSizeChanged { heightPx = it.height }
            .graphicsLayer {
                alpha = if (visible) 1f else 0f
                translationY = y.value
                translationX = if (jitter) jitterPx else 0f
            }
            .testTag("paper_current"),
    )
}

@Composable
private fun DroppingPaper(workout: Workout, trayHeight: Float, motion: Boolean) {
    val density = LocalDensity.current
    val restPx = with(density) { PrinterTimings.restOffsetFor(workout).toPx() }
    val impulsePx = with(density) { PrinterTimings.impulse.toPx() }
    val endPx = trayHeight + with(density) { PrinterTimings.dropOvershoot.toPx() }
    val dpPx = density.density
    val p = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (motion) p.animateTo(1f, tween(DROP_MS, easing = LinearEasing)) else p.snapTo(1f)
    }

    PaperSlip(
        workout = workout,
        glow = { 0f },
        modifier = PaperModifier
            .graphicsLayer {
                val t = p.value * DROP_MS
                translationY = if (t < IMPULSE_MS) {
                    val q = t / IMPULSE_MS
                    restPx - impulsePx * (1f - (1f - q) * (1f - q))
                } else {
                    val q = PrinterTimings.gravity.transform((t - IMPULSE_MS) / (DROP_MS - IMPULSE_MS))
                    val apex = restPx - impulsePx
                    apex + (endPx - apex) * q
                }
                translationX = sway(p.value) * dpPx
            }
            .testTag("paper_discarding"),
    )
}

private fun sway(p: Float): Float {
    val points = PrinterTimings.swayX
    val e = 1f - (1f - p) * (1f - p)
    val pos = e * (points.size - 1)
    val i = pos.toInt().coerceAtMost(points.size - 2)
    val f = pos - i
    return points[i] + (points[i + 1] - points[i]) * f
}

private fun Modifier.trayBackground() = drawWithCache {
    val cx = size.width / 2f
    val cy = size.height * 0.34f
    val rx = size.width * 0.75f
    val ry = size.height * 0.6f
    val sy = ry / rx
    val brush = Brush.radialGradient(
        0f to Palette.TrayLight, 0.52f to Palette.TrayMid, 1f to Palette.TrayDark,
        center = Offset(cx, cy), radius = rx,
    )
    val top = cy - cy / sy
    val bottom = cy + (size.height - cy) / sy
    onDrawBehind {
        scale(1f, sy, pivot = Offset(cx, cy)) {
            drawRect(brush, topLeft = Offset(0f, top), size = Size(size.width, bottom - top))
        }
        scanlines(Color(0x05FFFFFF))
    }
}

@Composable
private fun Sink(modifier: Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                Brush.verticalGradient(
                    0f to Palette.Graphite.copy(alpha = 0f), 0.52f to Palette.Graphite, 1f to Palette.Graphite,
                ),
            )
            .drawBehind {
                val side = 18.dp.toPx()
                val h = 10.dp.toPx()
                val top = size.height - 16.dp.toPx() - h
                val grille = Path().apply {
                    addRoundRect(RoundRect(side, top, size.width - side, top + h, CornerRadius(5.dp.toPx())))
                }
                clipPath(grille) {
                    drawRect(Palette.Slot)
                    val bar = 2.dp.toPx()
                    val step = 8.dp.toPx()
                    var x = side + 6.dp.toPx()
                    while (x < size.width - side) {
                        drawRect(Color(0xFF1A1A1F), topLeft = Offset(x, top), size = Size(bar, h))
                        x += step
                    }
                    drawRect(
                        Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = top, endY = top + 3.dp.toPx()),
                        topLeft = Offset(0f, top), size = Size(size.width, 3.dp.toPx()),
                    )
                }
                drawRect(
                    Color(0x12FFFFFF),
                    topLeft = Offset(side + 4.dp.toPx(), top + h),
                    size = Size(size.width - 2 * side - 8.dp.toPx(), 1.dp.toPx()),
                )
            },
    )
}
