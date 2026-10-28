package com.vedovatto.setslip.ui.printer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vedovatto.setslip.domain.Workout
import com.vedovatto.setslip.ui.theme.Fonts
import com.vedovatto.setslip.ui.theme.Palette
import java.util.Random

private val Mono = TextStyle(fontFamily = Fonts.Mono, color = Palette.PaperText)

val Workout.isLong: Boolean
    get() = exerciseCount + (if (sections.size > 1) sections.size else 0) >= 10

@Composable
fun PaperSlip(workout: Workout, glow: () -> Float, modifier: Modifier = Modifier) {
    val grain = rememberPaperGrain()
    val multi = workout.sections.size > 1
    val compact = workout.isLong
    val rowPad = if (compact) 1.5.dp else 5.dp
    val rowSize = if (compact) 11.5.sp else 12.sp
    val gap = if (compact) 5.dp else 10.dp

    Column(
        modifier
            .softShadow(Color(0xAA000000), blur = 28.dp, offsetY = { 16.dp })
            .drawWithCache {
                val bg = Brush.verticalGradient(listOf(Palette.Paper, Palette.Paper2))
                val glowH = 26.dp.toPx()
                val glowBrush = Brush.verticalGradient(
                    listOf(Palette.Warm.copy(alpha = 0.33f), Color.Transparent), endY = glowH,
                )
                val scallop = 4.dp.toPx()
                val scallopStep = 12.dp.toPx()
                onDrawWithContent {
                    drawRect(Color(0x33000000), topLeft = Offset(0f, 2.dp.toPx()), size = size)
                    var x = 6.dp.toPx()
                    while (x < size.width) {
                        drawCircle(Palette.Paper2, scallop, Offset(x, size.height))
                        x += scallopStep
                    }
                    drawRect(bg)
                    drawRect(grain)
                    scanlines(Color(0x07000000))
                    drawContent()
                    val g = glow()
                    if (g > 0f) drawRect(glowBrush, size = Size(size.width, glowH), alpha = g)
                }
            }
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 20.dp),
    ) {
        BasicText(
            workout.title,
            style = Mono.copy(
                fontSize = if (multi) 12.sp else 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = if (multi) 0.06.em else 0.12.em,
            ),
        )
        Spacer(Modifier.height(2.dp))
        BasicText(
            "FICHA ${workout.variationLetter} · ${workout.exerciseCount} EXERCÍCIOS",
            style = Mono.copy(fontSize = 10.sp, color = Palette.PaperMuted, letterSpacing = 0.12.em),
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind { hairline(size.height - 0.5f, Color(0x40000000), 3.dp, 2.dp) }
                .padding(bottom = 8.dp),
        )
        Spacer(Modifier.height(6.dp))

        workout.sections.forEach { section ->
            if (multi) SectionLabel(section.group.label.uppercase(), top = if (compact) 5.dp else 9.dp)
            section.exercises.forEach { e ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .drawBehind { hairline(size.height - 0.5f, Color(0x2A000000), 1.dp, 2.dp) }
                        .padding(vertical = rowPad),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BasicText(
                        e.name,
                        style = Mono.copy(fontSize = rowSize, fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    BasicText(e.prescription, style = Mono.copy(fontSize = rowSize, color = Palette.PaperSoft))
                }
            }
        }

        BasicText(
            "+ CÁRDIO À ESCOLHA (OPCIONAL)",
            style = Mono.copy(fontSize = 10.sp, color = Palette.PaperMuted, letterSpacing = 0.1.em),
            modifier = Modifier
                .padding(top = gap)
                .fillMaxWidth()
                .drawBehind { hairline(0.5f, Color(0x40000000), 3.dp, 2.dp) }
                .padding(top = if (compact) 6.dp else 8.dp),
        )
        Barcode(
            seed = workout.serial,
            modifier = Modifier
                .padding(top = gap)
                .fillMaxWidth()
                .height(if (compact) 12.dp else 18.dp),
        )
    }
}

@Composable
private fun SectionLabel(text: String, top: Dp) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = top, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text,
            style = Mono.copy(fontSize = 9.5.sp, color = Palette.PaperMuted, letterSpacing = 0.2.em),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .drawBehind { drawRect(Color(0x2E000000)) },
        )
    }
}

@Composable
private fun Barcode(seed: Int, modifier: Modifier) {
    Box(
        modifier.drawBehind {
            val rnd = Random(seed.toLong() * 31 + 7)
            val unit = 1.dp.toPx()
            var x = 0f
            while (x < size.width) {
                val bar = unit * (1 + rnd.nextInt(3))
                drawRect(Palette.PaperText, topLeft = Offset(x, 0f), size = Size(minOf(bar, size.width - x), size.height))
                x += bar + unit * (1 + rnd.nextInt(3))
            }
        },
    )
}
