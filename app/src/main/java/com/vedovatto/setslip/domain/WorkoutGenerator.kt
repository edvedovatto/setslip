package com.vedovatto.setslip.domain

const val VARIATIONS = 3

class WorkoutGenerator(private val catalog: Map<MuscleGroup, List<Exercise>>) {

    fun generate(
        serial: Int,
        groups: List<MuscleGroup>,
        variations: Map<MuscleGroup, Int>,
        struck: Set<String> = emptySet(),
    ): Workout {
        require(groups.isNotEmpty() && groups.size <= MAX_GROUPS) { "expected 1 to $MAX_GROUPS groups" }
        val ordered = groups.sortedWith(compareBy({ it.size.ordinal }, { it.ordinal }))
        require(exhausted(ordered, struck).isEmpty()) { "every selected group needs an available exercise" }
        val sections = ordered.map { group ->
            val pool = available(group, struck).rotated(variationOf(group, variations) * 2)
            WorkoutSection(group, pool.take(countFor(group, ordered)))
        }
        return Workout(serial, ordered, variationOf(ordered.first(), variations), sections)
    }

    fun exhausted(groups: List<MuscleGroup>, struck: Set<String>): List<MuscleGroup> =
        groups.filter { available(it, struck).isEmpty() }

    private fun available(group: MuscleGroup, struck: Set<String>) =
        catalog.getValue(group).filter { it.id !in struck }

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
