package com.vedovatto.setslip.ui.printer

import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.data.Prefs
import com.vedovatto.setslip.domain.MuscleGroup.*
import com.vedovatto.setslip.domain.WorkoutGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrinterUiStateTest {

    private val slip = WorkoutGenerator(Catalog.exercises).generate(1, listOf(BICEPS, QUADRICEPS), emptyMap())

    @Test fun `groups already on the slip cannot be printed again, whatever the order`() {
        assertEquals(listOf(QUADRICEPS, BICEPS), slip.groups)
        assertFalse(PrinterUiState(phase = PrinterPhase.READY, selection = listOf(BICEPS, QUADRICEPS), current = slip).canPrint)
        assertFalse(PrinterUiState(phase = PrinterPhase.READY, selection = listOf(QUADRICEPS, BICEPS), current = slip).canPrint)
    }

    @Test fun `a different selection can be printed`() {
        assertTrue(PrinterUiState(selection = listOf(CHEST)).canPrint)
        assertTrue(PrinterUiState(phase = PrinterPhase.READY, selection = listOf(BICEPS), current = slip).canPrint)
        assertTrue(PrinterUiState(phase = PrinterPhase.READY, selection = listOf(BICEPS, QUADRICEPS, ABS), current = slip).canPrint)
    }

    @Test fun `nothing prints with an empty selection or a busy printer`() {
        assertFalse(PrinterUiState().canPrint)
        assertFalse(PrinterUiState(phase = PrinterPhase.PRINTING, selection = listOf(CHEST)).canPrint)
        assertFalse(PrinterUiState(phase = PrinterPhase.REPLACING, selection = listOf(CHEST), current = slip).canPrint)
    }

    @Test fun `reprint needs a slip and an idle printer`() {
        assertFalse(PrinterUiState().canReprint)
        assertTrue(PrinterUiState(phase = PrinterPhase.READY, current = slip).canReprint)
        assertFalse(PrinterUiState(phase = PrinterPhase.REPLACING, current = slip).canReprint)
    }

    @Test fun `favorite follows the saved keys`() {
        assertFalse(PrinterUiState(current = slip).isFavorite)
        assertTrue(PrinterUiState(current = slip, prefs = Prefs(favorites = listOf(slip.key))).isFavorite)
    }
}
