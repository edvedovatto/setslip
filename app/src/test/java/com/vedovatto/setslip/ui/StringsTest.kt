package com.vedovatto.setslip.ui

import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.domain.Equipment
import com.vedovatto.setslip.domain.Language
import com.vedovatto.setslip.domain.Level
import com.vedovatto.setslip.domain.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringsTest {

    private val all = listOf(PtStrings, EnStrings)

    @Test fun `todo grupo equipamento e nivel tem texto nos dois idiomas`() = all.forEach { s ->
        MuscleGroup.entries.forEach { assertTrue(s.group(it).isNotBlank() && s.code(it).length == 3) }
        Equipment.entries.forEach { assertTrue(s.equipment(it).isNotBlank()) }
        Level.entries.forEach { assertTrue(s.level(it).isNotBlank()) }
    }

    @Test fun `todo exercicio tem passos nos dois idiomas`() = Catalog.byId.values.forEach {
        assertEquals(it.id, it.steps.size, it.stepsEn.size)
        assertTrue(it.id, it.stepsEn.all { s -> s.isNotBlank() })
    }

    @Test fun `contador formata nos dois idiomas`() {
        assertEquals("2 DE 6 FEITOS", PtStrings.done(2, 6))
        assertEquals("2 OF 6 DONE", EnStrings.done(2, 6))
    }

    @Test fun `idioma escolhe o texto certo`() {
        assertEquals(PtStrings, stringsFor(Language.PT))
        assertEquals(EnStrings, stringsFor(Language.EN))
    }
}
