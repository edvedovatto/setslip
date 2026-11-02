package com.vedovatto.setslip.domain

const val VARIATIONS = 3

class WorkoutGenerator(private val catalog: Map<MuscleGroup, List<Exercise>>) {

    fun generate(
        serial: Int,
        groups: List<MuscleGroup>,
        variations: Map<MuscleGroup, Int>,
        struck: Set<String> = emptySet(),
    ): Workout {
        require(groups.isNotEmpty() && groups.size <= MAX_GROUPS) { "1 a $MAX_GROUPS grupos" }
        val ordered = groups.sortedWith(compareBy({ it.size.ordinal }, { it.ordinal }))
        val used = mutableSetOf<String>()
        val sections = ordered.map { group ->
            val (kept, dropped) = catalog.getValue(group).partition { it.id !in struck }
            val pool = (kept + dropped).rotated(variationOf(group, variations) * 2)
            val picked = pool.filter { used.add(it.name) }.take(countFor(group, ordered))
            WorkoutSection(group, picked)
        }
        return Workout(serial, ordered, variationOf(ordered.first(), variations), sections)
    }

    private fun variationOf(group: MuscleGroup, variations: Map<MuscleGroup, Int>) =
        (variations[group] ?: 0).mod(VARIATIONS)

    companion object {
        fun countFor(group: MuscleGroup, groups: List<MuscleGroup>): Int = when {
            groups.size == 1 -> 6
            groups.none { it.size == GroupSize.LARGE } -> 3
            group.size == GroupSize.LARGE -> 3
            else -> 2
        }
    }
}

private fun <T> List<T>.rotated(by: Int): List<T> {
    if (isEmpty()) return this
    val k = by.mod(size)
    return drop(k) + take(k)
}
