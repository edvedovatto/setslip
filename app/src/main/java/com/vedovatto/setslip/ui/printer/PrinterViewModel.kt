package com.vedovatto.setslip.ui.printer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.data.Prefs
import com.vedovatto.setslip.data.SettingsStore
import com.vedovatto.setslip.domain.Language
import com.vedovatto.setslip.domain.MAX_GROUPS
import com.vedovatto.setslip.domain.MuscleGroup
import com.vedovatto.setslip.domain.ToggleResult
import com.vedovatto.setslip.domain.Workout
import com.vedovatto.setslip.domain.WorkoutGenerator
import com.vedovatto.setslip.domain.toggle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PrinterPhase(val description: String) {
    IDLE("repouso"),
    PRINTING("imprimindo"),
    READY("pronta"),
    REPLACING("substituindo"),
}

sealed interface Screen {
    data object Printer : Screen
    data object Index : Screen
    data class Detail(val exerciseId: String) : Screen
    data object Favorites : Screen
    data object Settings : Screen
}

data class PrinterUiState(
    val phase: PrinterPhase = PrinterPhase.IDLE,
    val selection: List<MuscleGroup> = emptyList(),
    val current: Workout? = null,
    val discarding: Workout? = null,
    val rejected: MuscleGroup? = null,
    val rejectCount: Int = 0,
    val prefs: Prefs = Prefs(),
    val checked: Set<String> = emptySet(),
    val undoId: String? = null,
    val undoCount: Int = 0,
    val screen: Screen = Screen.Printer,
    val favoriteFilter: Set<MuscleGroup> = emptySet(),
) {
    val soundOn: Boolean get() = prefs.soundOn
    val language: Language get() = prefs.language
    val busy: Boolean get() = phase == PrinterPhase.PRINTING || phase == PrinterPhase.REPLACING
    val canPrint: Boolean get() = !busy && selection.isNotEmpty() && selection != current?.groups
    val canReprint: Boolean get() = !busy && current != null
    val isFavorite: Boolean get() = current?.key in prefs.favorites
    val activeExercises get() = current?.exercises.orEmpty().filter { it.id !in prefs.struck }
    val doneCount: Int get() = activeExercises.count { it.id in checked }
}

class PrinterViewModel(
    private val store: SettingsStore,
    private val generator: WorkoutGenerator = WorkoutGenerator(Catalog.exercises),
) : ViewModel() {

    private val _state = MutableStateFlow(PrinterUiState())
    val state: StateFlow<PrinterUiState> = _state.asStateFlow()

    private var serial = 0

    init {
        viewModelScope.launch { store.prefs.collect { p -> _state.update { it.copy(prefs = p) } } }
    }

    fun onKey(group: MuscleGroup) = _state.update { s ->
        if (s.busy) return@update s
        when (val r = s.selection.toggle(group)) {
            is ToggleResult.Changed -> s.copy(selection = r.selection)
            ToggleResult.Rejected -> s.copy(rejected = group, rejectCount = s.rejectCount + 1)
        }
    }

    fun onRandom() = _state.update { s ->
        if (s.busy) return@update s
        val free = MuscleGroup.entries - s.selection.toSet()
        if (s.selection.size >= MAX_GROUPS || free.isEmpty()) return@update s
        s.copy(selection = (s.selection + free.random()).sortedBy { it.ordinal })
    }

    fun onPrint() {
        val s = _state.value
        if (!s.canPrint) return
        emit(s, s.selection)
    }

    fun onReprint() {
        val s = _state.value
        if (!s.canReprint) return
        emit(s, s.current!!.groups)
    }

    fun onPrintFinished() = _state.update { s ->
        if (!s.busy) s else s.copy(phase = PrinterPhase.READY, discarding = null)
    }

    fun onToggleCheck(id: String) = _state.update { s ->
        if (s.phase != PrinterPhase.READY || id in s.prefs.struck) s
        else s.copy(checked = if (id in s.checked) s.checked - id else s.checked + id)
    }

    fun onStrike(id: String) {
        val s = _state.value
        if (s.phase != PrinterPhase.READY || id in s.prefs.struck) return
        _state.update { it.copy(checked = it.checked - id, undoId = id, undoCount = it.undoCount + 1) }
        persist { it.copy(struck = it.struck + id) }
    }

    fun onUndoStrike() {
        val id = _state.value.undoId ?: return
        _state.update { it.copy(undoId = null) }
        persist { it.copy(struck = it.struck - id) }
    }

    fun onDismissUndo() = _state.update { it.copy(undoId = null) }

    fun onUnstrike(id: String) = persist { it.copy(struck = it.struck - id) }

    fun onToggleFavorite() {
        val key = _state.value.current?.key ?: return
        persist { p ->
            p.copy(favorites = if (key in p.favorites) p.favorites - key else listOf(key) + p.favorites)
        }
    }

    fun onToggleFilter(group: MuscleGroup) = _state.update {
        it.copy(favoriteFilter = if (group in it.favoriteFilter) it.favoriteFilter - group else it.favoriteFilter + group)
    }

    fun onOpenFavorite(key: String) {
        val s = _state.value
        if (s.busy) return
        val workout = Workout.fromKey(++serial, key, Catalog.byId::get) ?: return
        _state.value = s.copy(
            phase = PrinterPhase.READY,
            selection = workout.groups,
            current = workout,
            discarding = null,
            checked = emptySet(),
            undoId = null,
            screen = Screen.Printer,
        )
    }

    fun onTipSeen() = persist { it.copy(tipSeen = true) }

    fun onToggleSound() = persist { it.copy(soundOn = !it.soundOn) }

    fun onToggleVibration() = persist { it.copy(vibrationOn = !it.vibrationOn) }

    fun onLanguage(language: Language) = persist { it.copy(language = language) }

    fun onNavigate(screen: Screen) = _state.update { it.copy(screen = screen) }

    fun onBack() = _state.update {
        it.copy(screen = if (it.screen is Screen.Detail) Screen.Index else Screen.Printer)
    }

    private fun persist(block: (Prefs) -> Prefs) {
        viewModelScope.launch { store.update(block) }
    }

    private fun emit(s: PrinterUiState, groups: List<MuscleGroup>) {
        val workout = generator.generate(++serial, groups, s.prefs.variations, s.prefs.struck)
        persist { p -> p.copy(variations = p.variations + groups.associateWith { (p.variations[it] ?: 0) + 1 }) }
        _state.value = s.copy(
            phase = if (s.current == null) PrinterPhase.PRINTING else PrinterPhase.REPLACING,
            selection = groups,
            current = workout,
            discarding = s.current,
            checked = emptySet(),
            undoId = null,
        )
    }
}
