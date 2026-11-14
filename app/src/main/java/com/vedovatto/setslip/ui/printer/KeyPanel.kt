package com.vedovatto.setslip.ui.printer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vedovatto.setslip.domain.MuscleGroup
import com.vedovatto.setslip.ui.Strings
import com.vedovatto.setslip.ui.printer.PrinterTimings.KEY_PRESS_MS
import com.vedovatto.setslip.ui.printer.PrinterTimings.REJECT_SHAKE_MS
import com.vedovatto.setslip.ui.printer.PrinterTimings.rejectShake
import com.vedovatto.setslip.ui.theme.Fonts
import com.vedovatto.setslip.ui.theme.Palette
import kotlinx.coroutines.delay

@Composable
fun KeyPanel(
    state: PrinterUiState,
    strings: Strings,
    haptics: Haptics,
    onKey: (MuscleGroup) -> Unit,
    onPrint: () -> Unit,
    onReprint: () -> Unit,
    onSettings: () -> Unit,
    onFavorites: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottom = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues().calculateBottomPadding()

    Column(
        modifier
            .panelSurface()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 26.dp + bottom),
    ) {
        MuscleGroup.entries.chunked(3).forEachIndexed { i, row ->
            if (i > 0) Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { group ->
                    GroupKey(
                        label = strings.code(group),
                        description = strings.group(group),
                        selected = group in state.selection,
                        shakeCount = if (state.rejected == group) state.rejectCount else 0,
                        flashCount = state.rejectCount,
                        haptics = haptics,
                        onClick = { onKey(group) },
                        enabled = !state.busy,
                        height = 42.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("key_${group.name.lowercase()}"),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconKey(
                description = strings.settings, tag = "btn_settings", enabled = true,
                haptics = haptics, onClick = onSettings,
            ) { gearIcon(Palette.CapText) }
            IconKey(
                description = strings.favorites, tag = "btn_favorites", enabled = true,
                haptics = haptics, onClick = onFavorites, badge = state.prefs.favorites.size,
            ) { starIcon(Palette.CapText, filled = false) }
            IconKey(
                description = strings.another, tag = "btn_another", enabled = state.canReprint,
                haptics = haptics, onClick = onReprint,
            ) { refreshIcon(Palette.CapText) }
            PanelButton(
                label = strings.print, glyph = "►", amber = true, enabled = state.canPrint,
                onClick = { haptics.key(); onPrint() },
                height = 46.dp,
                modifier = Modifier.weight(1f).testTag("btn_print"),
            )
        }
    }
}

@Composable
private fun IconKey(
    description: String,
    tag: String,
    enabled: Boolean,
    haptics: Haptics,
    onClick: () -> Unit,
    badge: Int = 0,
    draw: DrawScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val depth by animateFloatAsState(if (pressed) 1f else 0f, tween(KEY_PRESS_MS), label = "depth")
    Box(Modifier.width(46.dp)) {
        KeyCap(
            depth = { depth },
            height = 46.dp,
            corner = 11.dp,
            travel = 4.dp,
            side = 5.dp,
            top = Palette.CapTop,
            bottom = Palette.CapBottom,
            sideColor = Palette.CapSide,
            highlight = Color(0x33FFFFFF),
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = if (enabled) 1f else 0.5f
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                }
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = { haptics.key(); onClick() },
                )
                .semantics { contentDescription = description }
                .testTag(tag),
        ) {
            Box(Modifier.size(20.dp).drawBehind(draw))
        }
        if (badge > 0) {
            BasicText(
                badge.toString(),
                style = TextStyle(fontFamily = Fonts.Mono, fontWeight = FontWeight.Medium, fontSize = 9.sp, color = Palette.AmberText),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-5).dp)
                    .background(Palette.Warm, RoundedCornerShape(7.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                    .testTag("badge_favorites"),
            )
        }
    }
}

private fun DrawScope.screw(center: Offset) {
    val r = 3.5.dp.toPx()
    drawCircle(Color.Black, r, center + Offset(0f, 1.dp.toPx()))
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0xFF555555), Color(0xFF1A1A1D)),
            center = center + Offset(-0.3f * r, -0.4f * r),
            radius = r * 1.3f,
        ),
        r, center,
    )
}

@Composable
internal fun GroupKey(
    label: String,
    selected: Boolean,
    shakeCount: Int,
    flashCount: Int,
    haptics: Haptics,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    height: Dp = 36.dp,
    description: String = label,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val depth by animateFloatAsState(if (pressed || selected) 1f else 0f, tween(KEY_PRESS_MS), label = "depth")
    val shake = remember { Animatable(0f) }
    LaunchedEffect(shakeCount) {
        if (shakeCount == 0) {
            shake.snapTo(0f)
            return@LaunchedEffect
        }
        haptics.reject()
        shake.animateTo(0f, keyframes {
            durationMillis = REJECT_SHAKE_MS
            0f at 0
            -rejectShake.value at REJECT_SHAKE_MS / 4
            rejectShake.value at REJECT_SHAKE_MS * 3 / 4
        })
    }

    val flash = remember { Animatable(1f) }
    val isSelected by rememberUpdatedState(selected)
    LaunchedEffect(flashCount) {
        if (flashCount == 0 || !isSelected) return@LaunchedEffect
        repeat(2) {
            flash.snapTo(0.2f); delay(PrinterTimings.LIMIT_FLASH_MS)
            flash.snapTo(1f); delay(PrinterTimings.LIMIT_FLASH_MS)
        }
    }

    KeyCap(
        depth = { depth },
        height = height,
        corner = 9.dp,
        travel = 3.dp,
        side = 4.dp,
        top = Palette.CapTop,
        bottom = Palette.CapBottom,
        sideColor = Palette.CapSide,
        highlight = Color(0x33FFFFFF),
        modifier = modifier
            .graphicsLayer { translationX = shake.value.dp.toPx() }
            .toggleable(
                value = selected,
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { haptics.key(); onClick() },
            )
            .semantics { contentDescription = description },
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 9.dp)
                    .width(3.dp)
                    .height(16.dp)
                    .softShadow(
                        Palette.Warm, blur = 8.dp, corner = 1.5.dp,
                        alpha = { if (selected) flash.value else 0f },
                    )
                    .drawBehind {
                        drawRoundRect(
                            if (selected) Palette.Warm else Palette.KeyLedOff,
                            cornerRadius = CornerRadius(1.5.dp.toPx()),
                            alpha = if (selected) flash.value else 1f,
                        )
                    },
            )
            BasicText(
                label.uppercase(),
                maxLines = 1,
                style = TextStyle(
                    fontFamily = Fonts.Mono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 0.06.em,
                    color = if (selected) Color.White else Palette.CapText,
                    shadow = Shadow(Color.Black, Offset(0f, 3f), 0f),
                ),
            )
        }
    }
}

@Composable
internal fun PanelButton(
    label: String,
    glyph: String,
    amber: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    height: Dp = 58.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val target = when {
        !enabled -> 0.8f
        pressed -> 1f
        else -> 0f
    }
    val depth by animateFloatAsState(target, tween(KEY_PRESS_MS), label = "depth")
    val textColor = if (amber) Palette.AmberText else Palette.CapText
    val textShadow = if (amber) Shadow(Color(0x55FFFFFF), Offset(0f, 3f), 0f) else Shadow(Color.Black, Offset(0f, 3f), 0f)

    KeyCap(
        depth = { depth },
        height = height,
        corner = 13.dp,
        travel = 5.dp,
        side = 6.dp,
        top = if (amber) Palette.AmberTop else Palette.CapTop,
        bottom = if (amber) Palette.AmberBottom else Palette.CapBottom,
        sideColor = if (amber) Palette.AmberSide else Palette.CapSide,
        highlight = if (amber) Color(0xAAFFFFFF) else Color(0x33FFFFFF),
        modifier = modifier
            .graphicsLayer {
                alpha = if (enabled) 1f else 0.5f
                compositingStrategy = CompositingStrategy.ModulateAlpha
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val style = TextStyle(
                fontFamily = Fonts.Mono,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 0.2.em,
                color = textColor,
                shadow = textShadow,
            )
            BasicText(glyph, style = style.copy(letterSpacing = 0.em))
            Spacer(Modifier.width(10.dp))
            BasicText(label, style = style)
        }
    }
}

@Composable
internal fun KeyCap(
    depth: () -> Float,
    height: Dp,
    corner: Dp,
    travel: Dp,
    side: Dp,
    top: Color,
    bottom: Color,
    sideColor: Color,
    highlight: Color,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .height(height)
            .graphicsLayer { translationY = travel.toPx() * depth() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .softShadow(
                    Color(0xAA000000),
                    blur = side * 2,
                    corner = corner,
                    offsetY = { side + 3.dp - (travel + 1.dp) * depth() },
                )
                .drawBehind {
                    val r = CornerRadius(corner.toPx())
                    val sideNow = (side - travel * depth()).toPx()
                    drawRoundRect(sideColor, topLeft = Offset(0f, sideNow), size = size, cornerRadius = r)
                    drawRoundRect(Brush.verticalGradient(listOf(top, bottom)), cornerRadius = r)
                    val inset = corner.toPx() * 0.6f
                    drawRect(
                        highlight,
                        topLeft = Offset(inset, 0f),
                        size = Size(size.width - inset * 2, 1.dp.toPx()),
                        alpha = 1f - 0.35f * depth(),
                    )
                },
        )
        content()
    }
}

internal fun Modifier.panelSurface(): Modifier = this
    .fillMaxWidth()
    .softShadow(Color(0xAA000000), blur = 24.dp, offsetY = { (-10).dp })
    .background(Brush.verticalGradient(listOf(Palette.PanelTop, Palette.PanelBottom)))
    .drawBehind {
        drawRect(Color(0x14FFFFFF), size = Size(size.width, 1.dp.toPx()))
        screw(Offset(9.dp.toPx() + 3.5.dp.toPx(), 8.dp.toPx() + 3.5.dp.toPx()))
        screw(Offset(size.width - 9.dp.toPx() - 3.5.dp.toPx(), 8.dp.toPx() + 3.5.dp.toPx()))
    }
