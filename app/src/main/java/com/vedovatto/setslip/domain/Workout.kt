package com.vedovatto.setslip.domain

enum class Equipment { BARBELL, DUMBBELL, CABLE, MACHINE, BODY, EZ_BAR, KETTLEBELL, OTHER }

enum class Level { BEGINNER, INTERMEDIATE, EXPERT }

data class Exercise(
    val id: String,
    val name: String,
    val nameEn: String,
    val prescription: String,
    val equipment: Equipment,
    val level: Level,
    val muscles: List<String>,
    val steps: List<String>,
    val stepsEn: List<String>,
) {
    fun name(language: Language) = if (language == Language.EN) nameEn else name

    fun steps(language: Language) = if (language == Language.EN) stepsEn else steps
}

data class WorkoutSection(val group: MuscleGroup, val exercises: List<Exercise>)

data class Workout(
    val serial: Int,
    val groups: List<MuscleGroup>,
    val variation: Int,
    val sections: List<WorkoutSection>,
) {
    val variationLetter: Char get() = 'A' + variation
    val exercises: List<Exercise> get() = sections.flatMap { it.exercises }
    val exerciseCount: Int get() = sections.sumOf { it.exercises.size }

    val key: String
        get() = listOf(
            variation.toString(),
            groups.joinToString("+") { it.name },
            sections.joinToString(";") { s -> s.exercises.joinToString(",") { it.id } },
        ).joinToString("|")

    companion object {
        fun fromKey(serial: Int, key: String, lookup: (String) -> Exercise?): Workout? {
            val parts = key.split("|")
            if (parts.size != 3) return null
            val variation = parts[0].toIntOrNull()?.takeIf { it in 0 until VARIATIONS } ?: return null
            val groups = parts[1].split("+").map { n -> MuscleGroup.entries.firstOrNull { it.name == n } ?: return null }
            if (groups.size > MAX_GROUPS || groups.distinct().size != groups.size) return null
            val ids = parts[2].split(";")
            if (ids.size != groups.size) return null
            val sections = groups.zip(ids).map { (g, line) ->
                WorkoutSection(g, line.split(",").map { lookup(it) ?: return null })
            }
            return Workout(serial, groups, variation, sections)
        }
    }
}
