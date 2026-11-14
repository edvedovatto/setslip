package com.vedovatto.setslip.ui.printer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vedovatto.setslip.ui.Strings
import com.vedovatto.setslip.ui.printer.PrinterTimings.LED_BLINK_MS
import com.vedovatto.setslip.ui.printer.PrinterTimings.SLOT_GLOW_MS
import com.vedovatto.setslip.ui.theme.Fonts
import com.vedovatto.setslip.ui.theme.Palette
import kotlinx.coroutines.delay

@Composable
fun PrinterHeader(
    printing: Boolean,
    soundOn: Boolean,
    strings: Strings,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    val top = WindowInsets.safeDrawing.only(WindowInsetsSides.Top).asPaddingValues().calculateTopPadding()

    Box(
        modifier
            .fillMaxWidth()
            .height(34.dp + top)
            .softShadow(Color(0xBB000000), blur = 26.dp, offsetY = { 12.dp })
            .background(
                Brush.verticalGradient(
                    0f to Palette.PrinterTop, 0.55f to Palette.PrinterMid, 1f to Palette.PrinterBottom,
                ),
            )
            .drawWithContent {
                drawContent()
                drawRect(Color(0x26FFFFFF), size = Size(size.width, 1.dp.toPx()))
            },
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = top, bottom = 11.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (title != null) {
                BasicText(
                    title,
                    style = TextStyle(
                        fontFamily = Fonts.Mono, fontWeight = FontWeight.Medium, fontSize = 12.sp,
                        letterSpacing = 0.2.em, color = Palette.CapText,
                    ),
                    modifier = Modifier.semantics { heading() },
                )
            }
            Led(
                printing = printing,
                soundOn = soundOn,
                strings = strings,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        role = Role.Switch,
                        onClickLabel = if (soundOn) strings.mute else strings.unmute,
                        onClick = onToggleSound,
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
        Slot(
            printing = printing,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
                .height(11.dp),
        )
    }
}

@Composable
private fun Led(printing: Boolean, soundOn: Boolean, strings: Strings, modifier: Modifier) {
    val target = when {
        printing -> 1f
        soundOn -> 0.4f
        else -> 0f
    }
    val level by animateFloatAsState(target, tween(300), label = "led")
    val blink = remember { Animatable(1f) }
    LaunchedEffect(printing) {
        if (!printing) {
            blink.snapTo(1f)
            return@LaunchedEffect
        }
        while (true) {
            blink.snapTo(1f); delay(LED_BLINK_MS.toLong())
            blink.snapTo(0.4f); delay(LED_BLINK_MS.toLong())
        }
    }
    Box(
        modifier
            .testTag("led")
            .semantics {
                contentDescription = strings.sound
                stateDescription = if (soundOn) strings.on else strings.off
            }
            .size(9.dp)
            .softShadow(Palette.Warm, blur = 12.dp, corner = 4.5.dp, alpha = { level * blink.value })
            .drawBehind {
                val a = 1f - level * (1f - blink.value)
                drawCircle(lerp(Palette.LedOff, Palette.Warm, level).copy(alpha = a))
                if (level < 1f) {
                    drawCircle(
                        Brush.verticalGradient(listOf(Color(0x99000000), Color.Transparent)),
                        alpha = 1f - level,
                    )
                }
            },
    )
}

@Composable
private fun Slot(printing: Boolean, modifier: Modifier) {
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(printing) {
        if (!printing) {
            pulse.snapTo(0f)
            return@LaunchedEffect
        }
        pulse.snapTo(0.45f)
        while (true) {
            pulse.animateTo(1f, tween(SLOT_GLOW_MS, easing = FastOutSlowInEasing))
            pulse.animateTo(0.45f, tween(SLOT_GLOW_MS, easing = FastOutSlowInEasing))
        }
    }
    Box(
        modifier.drawBehind {
            val r = 6.dp.toPx()
            drawRect(Color(0x12FFFFFF), topLeft = Offset(0f, -1.dp.toPx()), size = Size(size.width, 1.dp.toPx()))
            val mouth = Path().apply {
                addRoundRect(
                    RoundRect(
                        0f, 0f, size.width, size.height,
                        topLeftCornerRadius = CornerRadius(r), topRightCornerRadius = CornerRadius(r),
                    ),
                )
            }
            drawPath(mouth, Palette.Slot)
            drawPath(
                mouth,
                Brush.verticalGradient(0f to Color.Black, 4.dp.toPx() / size.height to Color.Transparent),
            )
        },
    ) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 6.dp)
                .fillMaxWidth()
                .height(2.dp)
                .softShadow(Palette.Warm, blur = 14.dp, spread = 3.dp, alpha = { pulse.value * 0.9f })
                .drawBehind { drawRect(Palette.Warm, alpha = pulse.value * 0.9f) },
        )
    }
}
