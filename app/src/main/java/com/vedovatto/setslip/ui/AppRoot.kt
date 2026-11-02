package com.vedovatto.setslip.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.vedovatto.setslip.ui.screens.HeaderControls
import com.vedovatto.setslip.ui.screens.LocalHeaderControls
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.vedovatto.setslip.ui.screens.IndexScreen
import com.vedovatto.setslip.ui.screens.SettingsScreen

@Composable
fun AppRoot() {
    val appContext = LocalContext.current.applicationContext
    val vm: PrinterViewModel = viewModel(
        factory = viewModelFactory { initializer { PrinterViewModel(SettingsStore(appContext)) } },
    )
    val state by vm.state.collectAsState()
    val strings = stringsFor(state.language)
    val haptics = rememberHaptics(state.prefs.vibrationOn)
    var lastOverlay by remember { mutableStateOf<Screen>(Screen.Settings) }
    if (state.screen != Screen.Printer) lastOverlay = state.screen

    BackHandler(enabled = state.screen != Screen.Printer) { vm.onBack() }

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalHeaderControls provides HeaderControls(state.soundOn, vm::onToggleSound),
    ) {
        Box(Modifier.fillMaxSize()) {
            PrinterScreen(vm)
            AnimatedVisibility(
                visible = state.screen != Screen.Printer,
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 14 },
                exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { it / 14 },
            ) {
                when (val screen = lastOverlay) {
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
                        onBack = vm::onBack,
                    )
                    Screen.Printer -> Unit
                }
            }
        }
    }
}
