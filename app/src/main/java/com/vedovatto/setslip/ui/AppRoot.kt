package com.vedovatto.setslip.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.data.SettingsStore
import com.vedovatto.setslip.ui.printer.PrinterScreen
import com.vedovatto.setslip.ui.printer.PrinterViewModel
import com.vedovatto.setslip.ui.printer.Screen
import com.vedovatto.setslip.ui.printer.rememberHaptics
import com.vedovatto.setslip.ui.screens.DetailScreen
import com.vedovatto.setslip.ui.screens.FavoritesScreen
import com.vedovatto.setslip.ui.screens.HeaderControls
import com.vedovatto.setslip.ui.screens.IndexScreen
import com.vedovatto.setslip.ui.screens.LocalHeaderControls
import com.vedovatto.setslip.ui.screens.SettingsScreen
import com.vedovatto.setslip.ui.theme.Palette

private const val MAX_FONT_SCALE = 1.3f
private val MaxContentWidth = 520.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppRoot() {
    val appContext = LocalContext.current.applicationContext
    val vm: PrinterViewModel = viewModel(
        factory = viewModelFactory { initializer { PrinterViewModel(SettingsStore(appContext)) } },
    )
    val state by vm.state.collectAsState()
    val strings = stringsFor(state.language)
    val haptics = rememberHaptics(state.prefs.vibrationOn)
    val density = LocalDensity.current
    val overlayOpen = state.screen != Screen.Printer
    var lastOverlay by remember { mutableStateOf<Screen>(Screen.Settings) }
    SideEffect { if (overlayOpen) lastOverlay = state.screen }
    val headerControls = remember(state.soundOn) { HeaderControls(state.soundOn, vm::onToggleSound) }

    BackHandler(enabled = overlayOpen) { vm.onBack() }

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalHeaderControls provides headerControls,
        LocalDensity provides Density(density.density, minOf(density.fontScale, MAX_FONT_SCALE)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Palette.Graphite)
                .semantics { testTagsAsResourceId = true },
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(Modifier.widthIn(max = MaxContentWidth).fillMaxHeight()) {
                Box(if (overlayOpen) Modifier.clearAndSetSemantics { } else Modifier) {
                    PrinterScreen(vm)
                }
                AnimatedVisibility(
                    visible = overlayOpen,
                    enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 14 },
                    exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { it / 14 },
                ) {
                    when (val screen = if (overlayOpen) state.screen else lastOverlay) {
                        Screen.Index -> state.current?.let { workout ->
                            IndexScreen(
                                workout = workout,
                                strings = strings,
                                haptics = haptics,
                                onOpen = { vm.onNavigate(Screen.Detail(it.id)) },
                                onBack = vm::onBack,
                            )
                        }
                        is Screen.Detail -> Catalog.byId[screen.exerciseId]?.let {
                            DetailScreen(it, strings, haptics, vm::onBack)
                        }
                        Screen.Favorites -> FavoritesScreen(
                            favorites = state.prefs.favorites,
                            filter = state.favoriteFilter,
                            busy = state.busy,
                            strings = strings,
                            haptics = haptics,
                            onToggleFilter = vm::onToggleFilter,
                            onOpen = vm::onOpenFavorite,
                            onBack = vm::onBack,
                        )
                        Screen.Settings -> SettingsScreen(
                            soundOn = state.soundOn,
                            vibrationOn = state.prefs.vibrationOn,
                            language = state.language,
                            struck = state.prefs.struck,
                            strings = strings,
                            haptics = haptics,
                            onLanguage = vm::onLanguage,
                            onToggleSound = vm::onToggleSound,
                            onToggleVibration = vm::onToggleVibration,
                            onUnstrike = vm::onUnstrike,
                            onUnstrikeAll = vm::onUnstrikeAll,
                            onBack = vm::onBack,
                        )
                        Screen.Printer -> Unit
                    }
                }
            }
        }
    }
}
