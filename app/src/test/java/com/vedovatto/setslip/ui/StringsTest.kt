package com.vedovatto.setslip.ui

import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.domain.Equipment
import com.vedovatto.setslip.domain.Language
import com.vedovatto.setslip.domain.Level
import com.vedovatto.setslip.domain.MuscleGroup
import com.vedovatto.setslip.ui.printer.PrinterPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringsTest {

    private val all = listOf(PtStrings, EnStrings)

    @Test fun `every group, equipment, level and phase has text in both languages`() = all.forEach { s ->
        MuscleGroup.entries.forEach { assertTrue(s.group(it).isNotBlank() && s.code(it).length == 3) }
        Equipment.entries.forEach { assertTrue(s.equipment(it).isNotBlank()) }
        Level.entries.forEach { assertTrue(s.level(it).isNotBlank()) }
        PrinterPhase.entries.forEach { assertTrue(s.phase(it).isNotBlank()) }
    }

    @Test fun `every exercise has steps in both languages`() = Catalog.byId.values.forEach {
        assertEquals(it.id, it.steps.size, it.stepsEn.size)
        assertTrue(it.id, it.stepsEn.all { s -> s.isNotBlank() })
    }

    @Test fun `counter is formatted in both languages`() {
        assertEquals("2 DE 6 FEITOS", PtStrings.done(2, 6))
        assertEquals("2 OF 6 DONE", EnStrings.done(2, 6))
    }

    @Test fun `counter says so when every exercise is crossed out`() {
        assertEquals("TUDO RISCADO", PtStrings.done(0, 0))
        assertEquals("ALL CROSSED OUT", EnStrings.done(0, 0))
    }

    @Test fun `exercise count agrees in number`() {
        assertEquals("1 EXERCÍCIO", PtStrings.exercises(1))
        assertEquals("6 EXERCÍCIOS", PtStrings.exercises(6))
        assertEquals("1 EXERCISE", EnStrings.exercises(1))
        assertEquals("6 EXERCISES", EnStrings.exercises(6))
    }

    @Test fun `exhausted groups are named in the notice`() {
        assertEquals("SEM EXERCÍCIOS: BÍCEPS, ABDÔMEN", PtStrings.noExercises(listOf(MuscleGroup.BICEPS, MuscleGroup.ABS)))
        assertEquals("NO EXERCISES LEFT: BICEPS", EnStrings.noExercises(listOf(MuscleGroup.BICEPS)))
    }

    @Test fun `language picks the matching strings`() {
        assertEquals(PtStrings, stringsFor(Language.PT))
        assertEquals(EnStrings, stringsFor(Language.EN))
    }
}
