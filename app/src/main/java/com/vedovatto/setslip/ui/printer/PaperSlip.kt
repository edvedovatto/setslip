package com.vedovatto.setslip.ui.printer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vedovatto.setslip.domain.Workout
import com.vedovatto.setslip.ui.LocalStrings
import com.vedovatto.setslip.ui.theme.Fonts
import com.vedovatto.setslip.ui.theme.Palette
import java.util.Random

private val Mono = TextStyle(fontFamily = Fonts.Mono, color = Palette.PaperText)

val Workout.isLong: Boolean
    get() = exerciseCount + (if (sections.size > 1) sections.size else 0) >= 8

class SlipInteraction(
    val checked: Set<String>,
    val struck: Set<String>,
    val favorite: Boolean,
    val haptics: Haptics,
    val onCheck: (String) -> Unit,
    val onStrike: (String) -> Unit,
    val onFavorite: () -> Unit,
    val onHowTo: () -> Unit,
    val onShare: () -> Unit,
    val sharing: Boolean = false,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaperSlip(
    workout: Workout,
    glow: () -> Float,
    modifier: Modifier = Modifier,
    interaction: SlipInteraction? = null,
    preview: Boolean = false,
) {
    val strings = LocalStrings.current
    val multi = workout.sections.size > 1
    val rowSize = 13.sp
    val gap = 9.dp
    val struck = interaction?.struck.orEmpty()
    val total = workout.exercises.count { it.id !in struck }
    val done = workout.exercises.count { it.id !in struck && it.id in interaction?.checked.orEmpty() }

    Column(
        modifier
            .paperBackground(glow)
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
    ) {
        val title = strings.title(workout)
        val longTitle = title.length > 20
        BasicText(
            title,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = if (longTitle) 12.sp else 15.sp, stepSize = 0.5.sp),
            style = Mono.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = if (longTitle) 0.06.em else 0.12.em,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp).semantics { heading() },
        )
        Row(
            Modifier
                .fillMaxWidth()
                .drawBehind { hairline(size.height - 0.5f, Color(0x40000000), 3.dp, 2.dp) }
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                buildAnnotatedString {
                    append("${strings.slip} ${workout.variationLetter} · ")
                    if (interaction != null) {
                        withStyle(SpanStyle(color = Color(0xFF2B4A8A))) { append(strings.done(done, total)) }
                    } else append(strings.exercises(workout.exerciseCount))
                },
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = 10.sp, stepSize = 0.5.sp),
                style = Mono.copy(color = Palette.PaperMuted, letterSpacing = 0.04.em),
                modifier = Modifier
                    .weight(1f)
                    .testTag("slip_counter"),
            )
            if (interaction != null && !interaction.sharing) {
                Row(Modifier.offset(x = 8.dp)) {
                    SlipAction(strings.howTo, "btn_how_to", interaction.haptics, interaction.onHowTo) {
                        Box(Modifier.size(21.dp).drawBehind { helpIcon(Palette.PaperText) })
                    }
                    SlipAction(
                        strings.favoriteSlip, "btn_favorite", interaction.haptics, interaction.onFavorite,
                        toggled = interaction.favorite,
                    ) {
                        Box(Modifier.size(21.dp).drawBehind { starIcon(if (interaction.favorite) FavoriteRed else Palette.PaperText, interaction.favorite) })
                    }
                    SlipAction(strings.share, "btn_share", interaction.haptics, interaction.onShare) {
                        Box(Modifier.size(21.dp).drawBehind { shareIcon(Palette.PaperText) })
                    }
                }
            }
        }
        Spacer(Modifier.height(2.dp))

        workout.sections.forEach { section ->
            if (multi) SectionLabel(strings.group(section.group).uppercase(), top = 8.dp)
            section.exercises.forEach { e ->
                val isStruck = e.id in struck
                val isChecked = e.id in interaction?.checked.orEmpty()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (interaction != null) {
                                Modifier
                                    .combinedClickable(
                                        interactionSource = null,
                                        indication = null,
                                        enabled = !isStruck,
                                        onClickLabel = strings.check,
                                        onLongClickLabel = strings.crossOut,
                                        onClick = { interaction.haptics.key(); interaction.onCheck(e.id) },
                                        onLongClick = { interaction.haptics.hold(); interaction.onStrike(e.id) },
                                    )
                                    .semantics {
                                        if (isStruck) {
                                            stateDescription = strings.crossedOut
                                        } else {
                                            role = Role.Checkbox
                                            toggleableState = if (isChecked) ToggleableState.On else ToggleableState.Off
                                        }
                                    }
                                    .testTag("row_${e.id}")
                            } else Modifier,
                        )
                        .drawBehind { hairline(size.height - 0.5f, Color(0x2A000000), 1.dp, 2.dp) }
                        .heightIn(min = 29.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (interaction != null || preview) CheckBox(checked = isChecked, struck = isStruck)
                    BasicText(
                        e.name(strings.language),
                        style = Mono.copy(
                            fontSize = rowSize,
                            fontWeight = FontWeight.Medium,
                            color = if (isStruck) Palette.PaperMuted else if (isChecked) Palette.PaperSoft else Palette.PaperText,
                            textDecoration = if (isStruck) TextDecoration.LineThrough else null,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    BasicText(e.prescription, style = Mono.copy(fontSize = rowSize, color = Palette.PaperSoft))
                }
            }
        }

        BasicText(
            strings.cardio,
            style = Mono.copy(fontSize = 10.sp, color = Palette.PaperMuted, letterSpacing = 0.1.em),
            modifier = Modifier
                .padding(top = gap)
                .fillMaxWidth()
                .drawBehind { hairline(0.5f, Color(0x40000000), 3.dp, 2.dp) }
                .padding(top = 10.dp),
        )
        Barcode(
            seed = workout.serial,
            modifier = Modifier
                .padding(top = gap)
                .fillMaxWidth()
                .height(14.dp),
        )
    }
}

@Composable
private fun SlipAction(
    description: String,
    tag: String,
    haptics: Haptics,
    onClick: () -> Unit,
    toggled: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val press = { haptics.key(); onClick() }
    Box(
        Modifier
            .size(36.dp)
            .then(
                if (toggled != null) {
                    Modifier.toggleable(
                        value = toggled,
                        interactionSource = null,
                        indication = null,
                        role = Role.Switch,
                        onValueChange = { press() },
                    )
                } else {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = description,
                        onClick = press,
                    )
                },
            )
            .semantics { contentDescription = description }
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun CheckBox(checked: Boolean, struck: Boolean) {
    Box(
        Modifier
            .size(14.dp)
            .drawBehind {
                if (!struck) {
                    drawRoundRect(
                        Color(0x80000000),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                        style = Stroke(1.dp.toPx()),
                    )
                }
                if (checked && !struck) {
                    translate(1.dp.toPx(), (-2).dp.toPx()) {
                        scale(1.1f, 1.1f) {
                            checkIcon(PenBlue, 1.8.dp.toPx())
                        }
                    }
                }
            },
    )
}

private val PenBlue = Color(0xFF2745A8)
private val FavoriteRed = Color(0xFFA0522D)

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
            modifier = Modifier.semantics { heading() },
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

fun Modifier.paperBackground(glow: () -> Float = { 0f }): Modifier = this
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
            drawRect(PaperGrain)
            scanlines(Color(0x07000000))
            drawContent()
            val g = glow()
            if (g > 0f) drawRect(glowBrush, size = Size(size.width, glowH), alpha = g)
        }
    }
