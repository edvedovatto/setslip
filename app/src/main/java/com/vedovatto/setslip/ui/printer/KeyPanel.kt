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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay
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
import com.vedovatto.setslip.ui.printer.PrinterTimings.KEY_PRESS_MS
import com.vedovatto.setslip.ui.printer.PrinterTimings.REJECT_SHAKE_MS
import com.vedovatto.setslip.ui.printer.PrinterTimings.rejectShake
import com.vedovatto.setslip.ui.theme.Fonts
import com.vedovatto.setslip.ui.theme.Palette

@Composable
fun KeyPanel(
    state: PrinterUiState,
    haptics: Haptics,
    onKey: (MuscleGroup) -> Unit,
    onPrint: () -> Unit,
    onReprint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottom = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues().calculateBottomPadding()

    Column(
        modifier
            .fillMaxWidth()
            .softShadow(Color(0xAA000000), blur = 24.dp, offsetY = { (-10).dp })
            .background(Brush.verticalGradient(listOf(Palette.PanelTop, Palette.PanelBottom)))
            .drawBehind {
                drawRect(Color(0x14FFFFFF), size = Size(size.width, 1.dp.toPx()))
                screw(Offset(9.dp.toPx() + 3.5.dp.toPx(), 8.dp.toPx() + 3.5.dp.toPx()))
                screw(Offset(size.width - 9.dp.toPx() - 3.5.dp.toPx(), 8.dp.toPx() + 3.5.dp.toPx()))
            }
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 36.dp + bottom),
    ) {
        MuscleGroup.entries.chunked(5).forEachIndexed { i, row ->
            if (i > 0) Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { group ->
                    SelectKey(
                        group = group,
                        selected = group in state.selection,
                        shakeCount = if (state.rejected == group) state.rejectCount else 0,
                        flashCount = state.rejectCount,
                        haptics = haptics,
                        onClick = { onKey(group) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PanelButton(
                label = "IMPRIMIR", glyph = "►", amber = true, enabled = state.canPrint,
                onClick = { haptics.key(); onPrint() },
                modifier = Modifier.weight(1.5f).testTag("btn_imprimir"),
            )
            PanelButton(
                label = "OUTRA", glyph = "↻", amber = false, enabled = state.canReprint,
                onClick = { haptics.key(); onReprint() },
                modifier = Modifier.weight(1f).testTag("btn_outra"),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.screw(center: Offset) {
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
private fun SelectKey(
    group: MuscleGroup,
    selected: Boolean,
    shakeCount: Int,
    flashCount: Int,
    haptics: Haptics,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val depth by animateFloatAsState(if (pressed || selected) 1f else 0f, tween(KEY_PRESS_MS), label = "depth")
    val shake = remember { Animatable(0f) }
    LaunchedEffect(shakeCount) {
        if (shakeCount == 0) return@LaunchedEffect
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
        height = 44.dp,
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
                role = Role.Checkbox,
                onValueChange = { haptics.key(); onClick() },
            )
            .semantics { contentDescription = group.label }
            .testTag("key_${group.code.lowercase()}"),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .width(18.dp)
                    .height(3.dp)
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
            Spacer(Modifier.height(6.dp))
            BasicText(
                group.code,
                style = TextStyle(
                    fontFamily = Fonts.Mono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 0.08.em,
                    color = if (selected) Color.White else Palette.CapText,
                    shadow = Shadow(Color.Black, Offset(0f, 3f), 0f),
                ),
            )
        }
    }
}

@Composable
private fun PanelButton(
    label: String,
    glyph: String,
    amber: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
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
        height = 58.dp,
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
private fun KeyCap(
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
