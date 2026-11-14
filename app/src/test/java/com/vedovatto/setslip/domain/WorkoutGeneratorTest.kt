package com.vedovatto.setslip.domain

import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.domain.MuscleGroup.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutGeneratorTest {

    private val generator = WorkoutGenerator(Catalog.exercises)

    private fun counts(vararg groups: MuscleGroup) =
        generator.generate(1, groups.toList(), emptyMap()).sections.map { it.group to it.exercises.size }

    private fun idsOf(group: MuscleGroup) = Catalog.exercises.getValue(group).map { it.id }

    @Test fun `single group yields six exercises`() = assertEquals(listOf(BICEPS to 6), counts(BICEPS))

    @Test fun `large groups get three and small get two, large first`() =
        assertEquals(listOf(BACK to 3, TRICEPS to 2), counts(TRICEPS, BACK))

    @Test fun `only small groups get three each`() =
        assertEquals(listOf(SHOULDERS to 3, BICEPS to 3, ABS to 3), counts(ABS, BICEPS, SHOULDERS))

    @Test fun `full leg day yields nine exercises`() =
        assertEquals(9, generator.generate(1, listOf(QUADRICEPS, HAMSTRINGS, GLUTES), emptyMap()).exerciseCount)

    @Test fun `no exercise repeats within a slip`() {
        MuscleGroup.entries.forEach { a ->
            MuscleGroup.entries.filter { it != a }.forEach { b ->
                val ids = generator.generate(1, listOf(a, b), emptyMap()).exercises.map { it.id }
                assertEquals(ids.size, ids.toSet().size)
            }
        }
    }

    @Test fun `next variation changes the slip`() {
        val a = generator.generate(1, listOf(CHEST), mapOf(CHEST to 0))
        val b = generator.generate(2, listOf(CHEST), mapOf(CHEST to 1))
        assertNotEquals(a.sections, b.sections)
        assertEquals('B', b.variationLetter)
    }

    @Test fun `variation counter wraps after the third slip`() {
        val first = generator.generate(1, listOf(CHEST), mapOf(CHEST to 0))
        val fourth = generator.generate(4, listOf(CHEST), mapOf(CHEST to VARIATIONS))
        assertEquals(first.sections, fourth.sections)
        assertEquals('A', fourth.variationLetter)
    }

    @Test fun `struck exercises never come back in any variation`() {
        val struck = idsOf(CHEST).take(2).toSet()
        repeat(VARIATIONS) { variation ->
            val slip = generator.generate(1, listOf(CHEST), mapOf(CHEST to variation), struck)
            assertTrue(slip.exercises.none { it.id in struck })
            assertEquals(6, slip.exerciseCount)
        }
    }

    @Test fun `slip gets shorter when few exercises are left`() {
        val struck = idsOf(ABS).drop(1).toSet()
        repeat(VARIATIONS) { variation ->
            val slip = generator.generate(1, listOf(ABS), mapOf(ABS to variation), struck)
            assertEquals(listOf(idsOf(ABS).first()), slip.exercises.map { it.id })
        }
    }

    @Test fun `group with every exercise struck is reported as exhausted`() {
        val struck = idsOf(BICEPS).toSet()
        assertEquals(listOf(BICEPS), generator.exhausted(listOf(CHEST, BICEPS), struck))
        assertEquals(emptyList<MuscleGroup>(), generator.exhausted(listOf(CHEST), struck))
    }

    @Test fun `generating with an exhausted group fails`() {
        assertThrows(IllegalArgumentException::class.java) {
            generator.generate(1, listOf(BICEPS), emptyMap(), idsOf(BICEPS).toSet())
        }
    }

    @Test fun `fourth key is rejected`() {
        val three = listOf(CHEST, BACK, ABS)
        assertTrue(three.toggle(GLUTES) is ToggleResult.Rejected)
        assertEquals(ToggleResult.Changed(listOf(CHEST, ABS)), three.toggle(BACK))
    }
}
