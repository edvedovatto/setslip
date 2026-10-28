package com.vedovatto.setslip.ui.printer

import androidx.lifecycle.ViewModel
import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.domain.MuscleGroup
import com.vedovatto.setslip.domain.ToggleResult
import com.vedovatto.setslip.domain.Workout
import com.vedovatto.setslip.domain.WorkoutGenerator
import com.vedovatto.setslip.domain.toggle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PrinterPhase(val description: String) {
    IDLE("repouso"),
    PRINTING("imprimindo"),
    READY("pronta"),
    REPLACING("substituindo"),
}

data class PrinterUiState(
    val phase: PrinterPhase = PrinterPhase.IDLE,
    val selection: List<MuscleGroup> = emptyList(),
    val current: Workout? = null,
    val discarding: Workout? = null,
    val rejected: MuscleGroup? = null,
    val rejectCount: Int = 0,
    val soundOn: Boolean = true,
) {
    val busy: Boolean get() = phase == PrinterPhase.PRINTING || phase == PrinterPhase.REPLACING
    val canPrint: Boolean get() = !busy && selection.isNotEmpty() && selection != current?.groups
    val canReprint: Boolean get() = !busy && current != null
}

class PrinterViewModel(
    private val generator: WorkoutGenerator = WorkoutGenerator(Catalog.exercises),
) : ViewModel() {

    private val _state = MutableStateFlow(PrinterUiState())
    val state: StateFlow<PrinterUiState> = _state.asStateFlow()

    private val variations = mutableMapOf<MuscleGroup, Int>()
    private var serial = 0

    fun onKey(group: MuscleGroup) = _state.update { s ->
        if (s.busy) return@update s
        when (val r = s.selection.toggle(group)) {
            is ToggleResult.Changed -> s.copy(selection = r.selection)
            ToggleResult.Rejected -> s.copy(rejected = group, rejectCount = s.rejectCount + 1)
        }
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

    fun onToggleSound() = _state.update { it.copy(soundOn = !it.soundOn) }

    fun onPrintFinished() = _state.update { s ->
        if (!s.busy) s else s.copy(phase = PrinterPhase.READY, discarding = null)
    }

    private fun emit(s: PrinterUiState, groups: List<MuscleGroup>) {
        val workout = generator.generate(++serial, groups, variations)
        groups.forEach { variations[it] = (variations[it] ?: 0) + 1 }
        _state.value = s.copy(
            phase = if (s.current == null) PrinterPhase.PRINTING else PrinterPhase.REPLACING,
            selection = groups,
            current = workout,
            discarding = s.current,
        )
    }
}
