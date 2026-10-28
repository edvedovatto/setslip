package com.vedovatto.setslip.domain

data class Exercise(val name: String, val prescription: String)

data class WorkoutSection(val group: MuscleGroup, val exercises: List<Exercise>)

data class Workout(
    val serial: Int,
    val groups: List<MuscleGroup>,
    val variation: Int,
    val sections: List<WorkoutSection>,
) {
    val title: String get() = groups.joinToString(" + ") { it.label.uppercase() }
    val variationLetter: Char get() = 'A' + variation
    val exerciseCount: Int get() = sections.sumOf { it.exercises.size }
}
