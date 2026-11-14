package com.vedovatto.setslip.ui.printer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.domain.Workout
import com.vedovatto.setslip.ui.LocalStrings
import com.vedovatto.setslip.ui.Strings
import com.vedovatto.setslip.ui.printer.PrinterTimings.DROP_MS
import com.vedovatto.setslip.ui.printer.PrinterTimings.FEED_MS
import com.vedovatto.setslip.ui.printer.PrinterTimings.IMPULSE_MS
import com.vedovatto.setslip.ui.theme.Fonts
import com.vedovatto.setslip.ui.theme.Palette
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val HANDOFF_MS = 100
private const val UNDO_MS = 4000L
private const val BLOCKED_MS = 6000L

@Composable
fun PrinterScreen(vm: PrinterViewModel) {
    val state by vm.state.collectAsState()
    val motion = rememberMotionEnabled()
    val strings = LocalStrings.current
    val haptics = rememberHaptics(state.prefs.vibrationOn)
    var feeding by remember { mutableStateOf(false) }
    val shareLayer = rememberGraphicsLayer()
    var sharing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
            .testTag("printer"),
    ) {
        PrinterHeader(
            printing = feeding,
            soundOn = state.soundOn,
            strings = strings,
            onToggleSound = vm::onToggleSound,
            modifier = Modifier.zIndex(2f),
        )
        PaperTray(
            state = state,
            motion = motion,
            haptics = haptics,
            interaction = SlipInteraction(
                checked = state.checked,
                struck = state.prefs.struck,
                favorite = state.isFavorite,
                haptics = haptics,
                onCheck = vm::onToggleCheck,
                onStrike = vm::onStrike,
                onFavorite = vm::onToggleFavorite,
                onHowTo = { if (!state.busy) vm.onNavigate(Screen.Index) },
                onShare = {
                    if (!state.busy && !sharing) scope.launch {
                        sharing = true
                        withFrameNanos { }
                        withFrameNanos { }
                        try {
                            shareSlip(context, shareLayer, strings.share)
                        } catch (_: IOException) {
                            haptics.reject()
                        } catch (_: ActivityNotFoundException) {
                            haptics.reject()
                        } finally {
                            sharing = false
                        }
                    }
                },
                sharing = sharing,
            ),
            shareLayer = shareLayer,
            onTipSeen = vm::onTipSeen,
            onUndo = vm::onUndoStrike,
            onDismissUndo = vm::onDismissUndo,
            onReactivate = vm::onReactivateBlocked,
            onDismissBlocked = vm::onDismissBlocked,
            onFeeding = { feeding = it },
            onFinished = vm::onPrintFinished,
            modifier = Modifier
                .weight(1f)
                .zIndex(1f),
        )
        KeyPanel(
            state = state,
            strings = strings,
            haptics = haptics,
            onKey = vm::onKey,
            onPrint = vm::onPrint,
            onReprint = vm::onReprint,
            onSettings = { vm.onNavigate(Screen.Settings) },
            onFavorites = { vm.onNavigate(Screen.Favorites) },
            modifier = Modifier.zIndex(3f),
        )
    }
}

@Composable
private fun PaperTray(
    state: PrinterUiState,
    motion: Boolean,
    haptics: Haptics,
    interaction: SlipInteraction,
    shareLayer: GraphicsLayer,
    onTipSeen: () -> Unit,
    onUndo: () -> Unit,
    onDismissUndo: () -> Unit,
    onReactivate: () -> Unit,
    onDismissBlocked: () -> Unit,
    onFeeding: (Boolean) -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier,
) {
    val strings = LocalStrings.current
    val accessibility = LocalAccessibilityManager.current
    val status = if (state.rejected != null) strings.maxGroups else strings.phase(state.phase)
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
                Box(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 64.dp)
                        .testTag("paper_scroll"),
                ) {
                    FeedingPaper(
                        workout = w,
                        animate = state.busy,
                        delayMs = if (state.discarding != null) DROP_MS + HANDOFF_MS else 0,
                        motion = motion,
                        haptics = haptics,
                        interaction = interaction,
                        shareLayer = shareLayer,
                        onFeeding = onFeeding,
                        onFinished = onFinished,
                    )
                }
            }
        }
        if (state.current == null) {
            EmptyHint(strings.hintEmpty, Modifier.align(Alignment.TopCenter).padding(top = 26.dp, start = 30.dp, end = 30.dp))
        }
        Sink(Modifier.align(Alignment.BottomCenter))
        if (state.current != null && state.phase == PrinterPhase.READY && !state.prefs.tipSeen) {
            TipCard(strings, onTipSeen, Modifier.align(Alignment.BottomCenter).padding(bottom = 62.dp, start = 18.dp, end = 18.dp))
        }
        state.undoId?.let { id ->
            val exercise = Catalog.byId[id]
            if (exercise != null) {
                LaunchedEffect(state.undoCount) {
                    delay(accessibility.timeoutFor(UNDO_MS))
                    onDismissUndo()
                }
                Toast(
                    text = "${strings.crossedOut}: ${exercise.name(strings.language)}",
                    action = strings.undo,
                    tag = "toast_struck",
                    actionTag = "btn_undo",
                    onAction = { haptics.key(); onUndo() },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp, start = 24.dp, end = 24.dp),
                )
            }
        }
        if (state.blocked.isNotEmpty()) {
            LaunchedEffect(state.blockedCount) {
                haptics.reject()
                delay(accessibility.timeoutFor(BLOCKED_MS))
                onDismissBlocked()
            }
            Toast(
                text = strings.noExercises(state.blocked),
                action = strings.reactivate,
                tag = "toast_blocked",
                actionTag = "btn_reactivate",
                onAction = { haptics.key(); onReactivate() },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp, start = 24.dp, end = 24.dp),
            )
        }
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .size(1.dp)
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = status
                }
                .testTag("status"),
        )
    }
}

private fun AccessibilityManager?.timeoutFor(millis: Long) =
    this?.calculateRecommendedTimeoutMillis(millis, containsText = true, containsControls = true) ?: millis

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
    interaction: SlipInteraction,
    shareLayer: GraphicsLayer,
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
        interaction = interaction,
        modifier = PaperModifier
            .onSizeChanged { heightPx = it.height }
            .graphicsLayer {
                alpha = if (visible) 1f else 0f
                translationY = y.value
                translationX = if (jitter) jitterPx else 0f
            }
            .drawWithContent {
                shareLayer.record { this@drawWithContent.drawContent() }
                drawLayer(shareLayer)
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

internal fun Modifier.trayBackground() = drawWithCache {
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

@Composable
private fun Toast(text: String, action: String, tag: String, actionTag: String, onAction: () -> Unit, modifier: Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .softShadow(Color(0xAA000000), blur = 16.dp, corner = 8.dp, offsetY = { 6.dp })
            .background(Palette.PrinterMid, RoundedCornerShape(8.dp))
            .padding(start = 14.dp, end = 4.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFamily = Fonts.Mono, fontSize = 11.sp, color = Palette.CapText),
            modifier = Modifier.weight(1f),
        )
        BasicText(
            action,
            style = TextStyle(fontFamily = Fonts.Mono, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.12.em, color = Palette.Warm),
            modifier = Modifier
                .clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onAction)
                .testTag(actionTag)
                .padding(horizontal = 14.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun EmptyHint(text: String, modifier: Modifier) {
    val amber = Palette.Warm
    Column(
        modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    amber.copy(alpha = 0.55f),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 20.dp)
            .testTag("hint_empty"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(
            text,
            style = TextStyle(
                fontFamily = Fonts.Mono, fontSize = 12.sp, letterSpacing = 0.16.em, lineHeight = 20.sp,
                color = Color(0xFFCFC7B6), textAlign = TextAlign.Center,
            ),
        )
        Spacer(Modifier.height(12.dp))
        Box(Modifier.size(22.dp).drawBehind { arrowDownIcon(amber) })
    }
}

@Composable
private fun TipCard(strings: Strings, onDismiss: () -> Unit, modifier: Modifier) {
    val amber = Palette.Warm
    val icons: List<DrawScope.() -> Unit> = listOf(
        { checkIcon(amber, 2.dp.toPx()) },
        { handIcon(amber) },
        { starIcon(amber, filled = false) },
        { helpIcon(amber) },
    )
    Column(
        modifier
            .fillMaxWidth()
            .softShadow(Color(0xA6000000), blur = 22.dp, corner = 8.dp, offsetY = { 10.dp })
            .background(Color(0xFF1C1A17), RoundedCornerShape(8.dp))
            .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 12.dp)
            .testTag("toast_tip"),
    ) {
        BasicText(
            strings.tipTitle,
            style = TextStyle(fontFamily = Fonts.Mono, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 0.24.em, color = amber),
            modifier = Modifier.padding(bottom = 9.dp),
        )
        strings.tips.forEachIndexed { i, (key, rest) ->
            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(18.dp).drawBehind(icons[i]))
                Spacer(Modifier.width(10.dp))
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Medium)) { append(key) }
                        append(rest)
                    },
                    style = TextStyle(fontFamily = Fonts.Mono, fontSize = 11.sp, letterSpacing = 0.06.em, lineHeight = 15.sp, color = Color(0xFFEFE8D8)),
                )
            }
        }
        Box(
            Modifier
                .padding(top = 9.dp)
                .fillMaxWidth()
                .height(36.dp)
                .background(amber, RoundedCornerShape(5.dp))
                .clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onDismiss)
                .testTag("btn_got_it"),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                strings.gotIt,
                style = TextStyle(fontFamily = Fonts.Mono, fontWeight = FontWeight.Medium, fontSize = 10.5.sp, letterSpacing = 0.2.em, color = Palette.AmberText),
            )
        }
    }
}

private suspend fun shareSlip(context: Context, layer: GraphicsLayer, title: String) {
    val bitmap = layer.toImageBitmap().asAndroidBitmap()
    val file = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        File(dir, "setslip.png").also { f -> f.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
