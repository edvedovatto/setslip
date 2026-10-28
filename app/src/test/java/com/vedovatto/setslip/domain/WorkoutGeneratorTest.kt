package com.vedovatto.setslip.domain

import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.domain.MuscleGroup.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutGeneratorTest {

    private val generator = WorkoutGenerator(Catalog.exercises)

    private fun counts(vararg groups: MuscleGroup) =
        generator.generate(1, groups.toList(), emptyMap()).sections.map { it.group to it.exercises.size }

    @Test fun `um grupo rende 6`() = assertEquals(listOf(BICEPS to 6), counts(BICEPS))

    @Test fun `grandes 3 e pequenos 2, grandes primeiro`() =
        assertEquals(listOf(BACK to 3, TRICEPS to 2), counts(TRICEPS, BACK))

    @Test fun `so pequenos rendem 3 cada`() =
        assertEquals(listOf(SHOULDERS to 3, BICEPS to 3, ABS to 3), counts(ABS, BICEPS, SHOULDERS))

    @Test fun `perna completa rende 9`() =
        assertEquals(9, generator.generate(1, listOf(QUADRICEPS, HAMSTRINGS, GLUTES), emptyMap()).exerciseCount)

    @Test fun `nenhum exercicio repete na ficha`() {
        MuscleGroup.entries.forEach { a ->
            MuscleGroup.entries.filter { it != a }.forEach { b ->
                val names = generator.generate(1, listOf(a, b), emptyMap()).sections.flatMap { it.exercises }.map { it.name }
                assertEquals(names.size, names.toSet().size)
            }
        }
    }

    @Test fun `variacao seguinte muda a ficha`() {
        val a = generator.generate(1, listOf(CHEST), mapOf(CHEST to 0))
        val b = generator.generate(2, listOf(CHEST), mapOf(CHEST to 1))
        assertNotEquals(a.sections, b.sections)
        assertEquals('B', b.variationLetter)
    }

    @Test fun `quarta tecla e rejeitada`() {
        val three = listOf(CHEST, BACK, ABS)
        assertTrue(three.toggle(GLUTES) is ToggleResult.Rejected)
        assertEquals(ToggleResult.Changed(listOf(CHEST, ABS)), three.toggle(BACK))
    }
}
