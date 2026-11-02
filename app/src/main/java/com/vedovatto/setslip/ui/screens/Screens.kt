package com.vedovatto.setslip.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.vedovatto.setslip.ui.printer.PaperSlip
import androidx.compose.runtime.key
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.zIndex
import com.vedovatto.setslip.ui.printer.PrinterHeader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.domain.Exercise
import com.vedovatto.setslip.domain.Language
import com.vedovatto.setslip.domain.MuscleGroup
import com.vedovatto.setslip.domain.Workout
import com.vedovatto.setslip.ui.Strings
import com.vedovatto.setslip.ui.printer.GroupKey
import com.vedovatto.setslip.ui.printer.Haptics
import com.vedovatto.setslip.ui.printer.PanelButton
import com.vedovatto.setslip.ui.printer.backIcon
import com.vedovatto.setslip.ui.printer.hairline
import com.vedovatto.setslip.ui.printer.paperBackground
import com.vedovatto.setslip.ui.printer.panelSurface
import com.vedovatto.setslip.ui.printer.rememberPaperGrain
import com.vedovatto.setslip.ui.printer.softShadow
import com.vedovatto.setslip.ui.printer.trayBackground
import com.vedovatto.setslip.ui.theme.Fonts
import com.vedovatto.setslip.ui.theme.Palette

private val Mono = TextStyle(fontFamily = Fonts.Mono, color = Palette.PaperText)
private val Muted = Mono.copy(fontSize = 10.sp, color = Palette.PaperMuted, letterSpacing = 0.12.em)

class HeaderControls(val soundOn: Boolean, val onToggleSound: () -> Unit)

val LocalHeaderControls = compositionLocalOf { HeaderControls(true) {} }

@Composable
fun ScreenFrame(
    title: String,
    strings: Strings,
    haptics: Haptics,
    tag: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    primary: (@Composable () -> Unit)? = null,
    panel: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val controls = LocalHeaderControls.current
    val bottom = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues().calculateBottomPadding()
    Column(
        modifier
            .fillMaxSize()
            .background(Palette.Graphite)
            .testTag(tag),
    ) {
        PrinterHeader(
            printing = false,
            soundOn = controls.soundOn,
            strings = strings,
            onToggleSound = controls.onToggleSound,
            title = title,
            modifier = Modifier.zIndex(2f),
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .trayBackground(),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()).padding(start = 30.dp, end = 30.dp, top = 24.dp, bottom = 40.dp) else Modifier),
                content = content,
            )
        }
        Column(
            Modifier
                .panelSurface()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 28.dp + bottom),
        ) {
            panel()
            if (primary != null) primary() else PanelButton(
                label = strings.back, glyph = "◄", amber = false, enabled = true,
                onClick = { haptics.key(); onBack() },
                modifier = Modifier.fillMaxWidth().testTag("btn_voltar"),
            )
        }
    }
}

@Composable
private fun Paper(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val grain = rememberPaperGrain()
    Column(
        modifier
            .fillMaxWidth()
            .paperBackground(grain)
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 22.dp),
        content = content,
    )
}

@Composable
private fun PaperHeading(title: String, subtitle: String) {
    BasicText(title, style = Mono.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.12.em))
    Spacer(Modifier.height(2.dp))
    BasicText(
        subtitle,
        style = Muted,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind { hairline(size.height - 0.5f, Color(0x40000000), 3.dp, 2.dp) }
            .padding(bottom = 8.dp),
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
fun IndexScreen(
    workout: Workout,
    strings: Strings,
    haptics: Haptics,
    onOpen: (Exercise) -> Unit,
    onBack: () -> Unit,
) {
    ScreenFrame(strings.howTo, strings, haptics, "screen_indice", onBack) {
        Paper {
            PaperHeading(strings.title(workout), strings.indexHint.uppercase())
            workout.exercises.forEachIndexed { i, e ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(interactionSource = null, indication = null, role = Role.Button) { haptics.key(); onOpen(e) }
                        .testTag("index_${e.id}")
                        .drawBehind { hairline(size.height - 0.5f, Color(0x2A000000), 1.dp, 2.dp) }
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText("%02d".format(i + 1), style = Muted)
                    BasicText(
                        e.name(strings.language),
                        style = Mono.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    BasicText("›", style = Mono.copy(fontSize = 14.sp, color = Palette.PaperSoft))
                }
            }
        }
    }
}

@Composable
fun DetailScreen(exercise: Exercise, strings: Strings, haptics: Haptics, onBack: () -> Unit) {
    val context = LocalContext.current
    val resId = remember(exercise.id) {
        context.resources.getIdentifier(exercise.imageName, "drawable", context.packageName)
    }
    ScreenFrame(strings.howTo, strings, haptics, "screen_detalhe", onBack) {
        Paper {
            BasicText(
                exercise.name(strings.language).uppercase(),
                style = Mono.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.1.em),
                modifier = Modifier.testTag("detail_title"),
            )
            Spacer(Modifier.height(2.dp))
            BasicText(
                "${strings.prescription}: ${exercise.prescription}",
                style = Muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind { hairline(size.height - 0.5f, Color(0x40000000), 3.dp, 2.dp) }
                    .padding(bottom = 8.dp),
            )
            Spacer(Modifier.height(10.dp))
            if (resId != 0) {
                Image(
                    painter = painterResource(resId),
                    contentDescription = exercise.name(strings.language),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 2f)
                        .clip(RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.height(12.dp))
            }
            Fact(strings.level, strings.level(exercise.level).uppercase())
            Fact(strings.equipment, strings.equipment(exercise.equipment).uppercase())
            Fact(strings.muscles, exercise.muscles.joinToString(", ") { strings.muscle(it) }.uppercase())
            Spacer(Modifier.height(14.dp))
            BasicText(
                strings.steps,
                style = Muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind { hairline(0.5f, Color(0x40000000), 3.dp, 2.dp) }
                    .padding(top = 8.dp, bottom = 6.dp),
            )
            exercise.steps(strings.language).forEachIndexed { i, step ->
                Row(Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BasicText("%02d".format(i + 1), style = Mono.copy(fontSize = 11.sp, color = Palette.PaperMuted))
                    BasicText(step, style = Mono.copy(fontSize = 12.sp, lineHeight = 17.sp), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind { hairline(size.height - 0.5f, Color(0x2A000000), 1.dp, 2.dp) }
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicText(label, style = Muted, modifier = Modifier.width(96.dp))
        BasicText(value, style = Mono.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
    }
}

@Composable
fun FavoritesScreen(
    favorites: List<String>,
    filter: Set<MuscleGroup>,
    strings: Strings,
    haptics: Haptics,
    onToggleFilter: (MuscleGroup) -> Unit,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
) {
    val slips = remember(favorites) {
        favorites.mapNotNull { key -> Workout.fromKey(0, key, Catalog.byId::get)?.let { key to it } }
    }
    val shown = slips.filter { (_, w) -> filter.isEmpty() || w.groups.any { it in filter } }.asReversed()
    val grain = rememberPaperGrain()
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val current = shown.firstOrNull { it.first == selected }?.first ?: shown.firstOrNull()?.first
    val ordered = shown.sortedBy { if (it.first == current) 0 else 1 }

    ScreenFrame(
        title = strings.favorites,
        strings = strings,
        haptics = haptics,
        tag = "screen_favoritas",
        onBack = onBack,
        scrollable = false,
        primary = if (current == null) null else {
            {
                PanelButton(
                    label = strings.useSlip, glyph = "►", amber = true, enabled = true,
                    onClick = { haptics.key(); onOpen(current) },
                    modifier = Modifier.fillMaxWidth().testTag("btn_usar"),
                )
            }
        },
        panel = {
            BasicText(
                strings.filterHint,
                style = TextStyle(fontFamily = Fonts.Mono, fontSize = 9.5.sp, letterSpacing = 0.18.em, color = Palette.CapText.copy(alpha = 0.6f)),
            )
            Spacer(Modifier.height(10.dp))
            MuscleGroup.entries.chunked(3).forEach { row ->
                Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { g ->
                        GroupKey(
                            label = strings.code(g),
                            selected = g in filter,
                            shakeCount = 0,
                            flashCount = 0,
                            haptics = haptics,
                            onClick = { onToggleFilter(g) },
                            height = 34.dp,
                            modifier = Modifier.weight(1f).testTag("filter_${g.code.lowercase()}"),
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        },
    ) {
        if (shown.isEmpty()) {
            BasicText(
                if (slips.isEmpty()) strings.emptyFavorites else strings.emptyFilter,
                style = Mono.copy(fontSize = 12.sp, color = Palette.CapText.copy(alpha = 0.7f), lineHeight = 17.sp),
                modifier = Modifier.padding(start = 30.dp, top = 24.dp).testTag("favorites_empty"),
            )
        } else {
            key(current) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 30.dp, end = 30.dp, top = 24.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ordered.forEach { (key, w) ->
                    val open = key == current
                    Box(
                        Modifier
                            .testTag("fav_${w.groups.joinToString("_") { it.code.lowercase() }}_${w.variation}"),
                    ) {
                        if (open) {
                            PaperSlip(workout = w, glow = { 0f }, preview = true, modifier = Modifier.fillMaxWidth())
                        } else {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .paperBackground(grain)
                                    .clickable(interactionSource = null, indication = null, role = Role.Button) {
                                        haptics.key()
                                        selected = key
                                    }
                                    .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
                            ) {
                                BasicText(
                                    strings.title(w),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = Mono.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.08.em),
                                )
                                Spacer(Modifier.height(2.dp))
                                BasicText("${strings.slip} ${w.variationLetter} · ${strings.exercises(w.exerciseCount)}", style = Muted)
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    soundOn: Boolean,
    vibrationOn: Boolean,
    language: Language,
    struck: Set<String>,
    strings: Strings,
    haptics: Haptics,
    onLanguage: (Language) -> Unit,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onUnstrike: (String) -> Unit,
    onBack: () -> Unit,
) {
    ScreenFrame(strings.settings, strings, haptics, "screen_ajustes", onBack) {
        Paper {
            SettingLabel(strings.languageTitle)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                Language.entries.forEach { l ->
                    Chip(
                        text = l.native.uppercase(),
                        selected = l == language,
                        tag = "btn_idioma_${l.tag.lowercase()}",
                        onClick = { haptics.key(); onLanguage(l) },
                    )
                }
            }
            SettingToggle(strings.sound, if (soundOn) strings.on else strings.off, soundOn, "toggle_som") { haptics.key(); onToggleSound() }
            SettingToggle(strings.vibration, if (vibrationOn) strings.on else strings.off, vibrationOn, "toggle_vibracao") { haptics.key(); onToggleVibration() }

            Spacer(Modifier.height(14.dp))
            SettingLabel(strings.crossedOutTitle)
            val items = struck.mapNotNull { Catalog.byId[it] }.sortedBy { it.name }
            if (items.isEmpty()) {
                BasicText(
                    strings.crossedOutEmpty,
                    style = Mono.copy(fontSize = 11.sp, color = Palette.PaperSoft),
                    modifier = Modifier.padding(vertical = 8.dp).testTag("struck_empty"),
                )
            } else {
                items.forEach { e ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(interactionSource = null, indication = null, role = Role.Button) { haptics.key(); onUnstrike(e.id) }
                            .testTag("struck_${e.id}")
                            .drawBehind { hairline(size.height - 0.5f, Color(0x2A000000), 1.dp, 2.dp) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        BasicText(
                            e.name(strings.language),
                            style = Mono.copy(fontSize = 11.5.sp, color = Palette.PaperMuted, textDecoration = TextDecoration.LineThrough),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        BasicText(strings.crossedOutHint, style = Muted)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            SettingLabel(strings.about)
            BasicText(strings.aboutText, style = Mono.copy(fontSize = 11.sp, lineHeight = 16.sp), modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(12.dp))
            SettingLabel(strings.credits)
            BasicText(strings.creditsText, style = Mono.copy(fontSize = 11.sp, lineHeight = 16.sp), modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun SettingLabel(text: String) {
    BasicText(
        text,
        style = Muted,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind { hairline(size.height - 0.5f, Color(0x40000000), 3.dp, 2.dp) }
            .padding(bottom = 6.dp),
    )
}

@Composable
private fun SettingToggle(label: String, value: String, on: Boolean, tag: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = null, indication = null, role = Role.Switch, onClick = onClick)
            .testTag(tag)
            .drawBehind { hairline(size.height - 0.5f, Color(0x2A000000), 1.dp, 2.dp) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(label, style = Mono.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
        BasicText(
            value,
            style = Mono.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (on) Palette.PaperText else Palette.PaperMuted),
        )
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clickable(interactionSource = null, indication = null, role = Role.RadioButton, onClick = onClick)
            .testTag(tag)
            .drawBehind {
                if (selected) drawRoundRect(Palette.PaperText, cornerRadius = CornerRadius(3.dp.toPx()))
                else drawRoundRect(Palette.PaperText, cornerRadius = CornerRadius(3.dp.toPx()), style = Stroke(1.dp.toPx()))
            }
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        BasicText(
            text,
            style = Mono.copy(
                fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.12.em,
                color = if (selected) Palette.Paper else Palette.PaperText,
            ),
        )
    }
}
