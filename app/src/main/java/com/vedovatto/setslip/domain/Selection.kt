package com.vedovatto.setslip.domain

const val MAX_GROUPS = 3

sealed interface ToggleResult {
    data class Changed(val selection: List<MuscleGroup>) : ToggleResult
    data object Rejected : ToggleResult
}

fun List<MuscleGroup>.toggle(group: MuscleGroup): ToggleResult = when {
    group in this -> ToggleResult.Changed(this - group)
    size >= MAX_GROUPS -> ToggleResult.Rejected
    else -> ToggleResult.Changed((this + group).sortedBy { it.ordinal })
}
